package service;

import model.Shift;
import model.ShiftType;
import model.Workplace;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * CSV 저장/로드 전담 서비스
 *
 * 역할:
 * 1. Workplace 목록 CSV 저장/로드
 * 2. Shift 목록 CSV 저장/로드
 * 3. Shift 로드 시 workplaceId를 이용해 실제 Workplace 객체와 연결
 *
 * 주의:
 * - 모든 문자열 필드는 반드시 큰따옴표로 감싼다.
 * - 문자열 안의 큰따옴표는 "" 로 이스케이프한다.
 */
public class StorageService {

    // 날짜/시간 저장 포맷
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Workplace 목록 저장
     *
     * CSV 형식:
     * id,companyName,hourlyRate,standardDayHours,standardNightHours
     *
     * @param workplaces 저장할 근무지 목록
     * @param path 저장 경로
     */
    public void saveWorkplaces(List<Workplace> workplaces, Path path) {
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("id,companyName,hourlyRate,standardDayHours,standardNightHours");
            writer.newLine();

            for (Workplace workplace : workplaces) {
                writer.write(
                        escapeCsv(workplace.getId()) + "," +
                        escapeCsv(workplace.getCompanyName()) + "," +
                        workplace.getHourlyRate() + "," +
                        workplace.getStandardDayHours() + "," +
                        workplace.getStandardNightHours()
                );
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Workplace CSV 저장 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * Shift 목록 저장
     *
     * CSV 형식:
     * id,workplaceId,startTime,endTime,shiftType
     *
     * @param shifts 저장할 근무 목록
     * @param path 저장 경로
     */
    public void saveShifts(List<Shift> shifts, Path path) {
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("id,workplaceId,startTime,endTime,shiftType");
            writer.newLine();

            for (Shift shift : shifts) {
                writer.write(
                        escapeCsv(shift.getId()) + "," +
                        escapeCsv(shift.getWorkplace().getId()) + "," +
                        escapeCsv(shift.getStartTime().format(FORMATTER)) + "," +
                        escapeCsv(shift.getEndTime().format(FORMATTER)) + "," +
                        shift.getShiftType().name()
                );
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Shift CSV 저장 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * Workplace 목록 로드
     *
     * @param path CSV 경로
     * @return 로드된 Workplace 목록
     */
    public List<Workplace> loadWorkplaces(Path path) {
        List<Workplace> workplaces = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                if (line.isBlank()) {
                    continue;
                }

                List<String> parts = parseCsvLine(line);

                Workplace workplace = new Workplace(
                        parts.get(0),
                        parts.get(1),
                        Long.parseLong(parts.get(2)),
                        Integer.parseInt(parts.get(3)),
                        Integer.parseInt(parts.get(4))
                );

                workplaces.add(workplace);
            }
        } catch (IOException e) {
            throw new RuntimeException("Workplace CSV 로드 중 오류가 발생했습니다.", e);
        }

        return workplaces;
    }

    /**
     * Shift 목록 로드
     *
     * 중요:
     * CSV의 workplaceId를 실제 Workplace 객체로 다시 연결한다.
     *
     * @param path CSV 경로
     * @param workplaces 메모리에 이미 로드된 Workplace 목록
     * @return 로드된 Shift 목록
     */
    public List<Shift> loadShifts(Path path, List<Workplace> workplaces) {
        List<Shift> shifts = new ArrayList<>();

        // workplaceId -> Workplace 매핑표 생성
        Map<String, Workplace> workplaceMap = new HashMap<>();
        for (Workplace workplace : workplaces) {
            workplaceMap.put(workplace.getId(), workplace);
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                if (line.isBlank()) {
                    continue;
                }

                List<String> parts = parseCsvLine(line);

                String shiftId = parts.get(0);
                String workplaceId = parts.get(1);
                LocalDateTime startTime = LocalDateTime.parse(parts.get(2), FORMATTER);
                LocalDateTime endTime = LocalDateTime.parse(parts.get(3), FORMATTER);
                ShiftType shiftType = ShiftType.valueOf(parts.get(4));

                Workplace workplace = workplaceMap.get(workplaceId);

                if (workplace == null) {
                    throw new IllegalStateException("연결할 Workplace를 찾을 수 없습니다. id=" + workplaceId);
                }

                Shift shift = new Shift(
                        shiftId,
                        workplace,
                        startTime,
                        endTime,
                        shiftType
                );

                shifts.add(shift);
            }
        } catch (IOException e) {
            throw new RuntimeException("Shift CSV 로드 중 오류가 발생했습니다.", e);
        }

        return shifts;
    }

    /**
     * CSV 문자열 이스케이프 처리
     *
     * 규칙:
     * - 모든 문자열은 큰따옴표로 감싼다.
     * - 문자열 내부의 큰따옴표는 "" 로 치환한다.
     *
     * 예:
     * Samsung, Electronics -> "Samsung, Electronics"
     * Test "Store" -> "Test ""Store"""
     *
     * @param value 원본 문자열
     * @return CSV 안전 문자열
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "\"\"";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    /**
     * CSV 한 줄 파싱
     *
     * 쉼표가 문자열 안에 들어가도 분리되지 않도록 직접 처리한다.
     *
     * @param line CSV 한 줄
     * @return 컬럼 값 리스트
     */
    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                // 문자열 내부의 "" 처리
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        result.add(current.toString());
        return result;
    }
}
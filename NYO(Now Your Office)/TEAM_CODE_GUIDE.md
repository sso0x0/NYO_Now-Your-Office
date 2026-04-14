# 팀원용 코드 수정 가이드

이 문서는 `NYO (근무 일정 / 급여 계산 프로그램)`를 처음 보는 팀원을 위한 안내서입니다.

목표는 두 가지입니다.

1. "이 기능은 어느 파일을 고쳐야 하는지" 바로 찾을 수 있게 하기
2. 실수로 건드리면 안 되는 부분과, 안전하게 수정해도 되는 부분을 구분해주기

---

## 변경 이력

### 2026-04-13

- 상단 중복 메뉴를 정리하고, 대시보드 내부 버튼 한 줄만 남기도록 구조를 단순화함
- 캘린더 날짜 클릭 시 바로 일정 등록창이 열리도록 변경함
- 일정 배지 클릭은 기존 일정 수정/삭제 전용으로 유지함
- 이 변경으로 같은 날짜에 여러 근무지를 각각 다른 일정으로 등록할 수 있게 개선함
- 하단 `오늘 일정 / 선택한 날짜 일정` 카드 높이를 늘려 내용이 잘리지 않도록 조정함
- 캘린더 셀에는 일정 2개까지만 직접 표시하고, 3개 이상이면 `+N개 더`로 요약 표시하도록 변경함
- 하단 상세 카드에는 `근무유형 + 시간 + 근무지명`이 함께 보이도록 개선함
- 전체 UI를 더 상용화된 프로그램처럼 보이도록 톤 다운함
- 사이드바를 카드형 섹션 구조로 정리하고, 버튼/목록/정보 카드의 스타일을 통일함
- 캘린더 카드, 일정 배지, 하단 상세 카드의 경계선/여백/색감을 정리해 PPT 느낌을 줄임
- 근무지 관리 버튼을 `추가 / 수정 / 삭제` 3개 가로 배치로 축소해 그래프 영역이 보이도록 조정함
- `SidebarPanel`에 팀원용 주석을 보강해 수정 포인트를 바로 찾을 수 있게 정리함
- 앞으로 기능 변경 시 이 문서에도 변경 내용을 함께 기록함

---

## 1. 전체 구조 한눈에 보기

이 프로젝트는 크게 4층으로 나뉩니다.

- `model`
  - 데이터 모양만 정의합니다.
  - 예: `Shift`, `Workplace`, `Post`
- `service`
  - 순수 계산 / 필터링 / 변환 로직이 들어 있습니다.
  - 예: 급여 계산, 근무지 검색, 일정 필터링
- `controller`
  - 버튼 클릭 이후 저장/삭제 흐름을 처리합니다.
  - 예: 일정 저장, 근무지 추가, 삭제 확인
- `ui`
  - 화면을 실제로 그리는 코드입니다.
  - 예: 로그인 화면, 달력, 마이페이지, 사이드바

쉽게 말하면:

- `ui`는 "보여주는 곳"
- `controller`는 "버튼 누른 뒤 연결하는 곳"
- `service`는 "실제 계산/판단하는 곳"
- `model`은 "데이터 자체"

---

## 2. 가장 중요한 파일

### `src/ui/WorkerCalendarAppFinal.java`

이 파일은 프로그램의 중심입니다.

- 프로그램 시작
- 로그인/회원가입
- 메인 화면 카드 전환
- 달력 새로고침
- 컨트롤러 연결
- 파일 저장/불러오기

초보 팀원 기준으로 가장 먼저 읽어야 할 파일입니다.

다만 이 파일은 크기 때문에 "다 한 번에 이해"하려고 하지 말고, 아래 순서로 보세요.

1. `createLoginPanel()`
2. `createDashboardPanel()`
3. `createSidebarPanel()`
4. `refreshCalendarView()`
5. `initializeControllers()`

---

## 3. 기능별로 어디를 고치면 되는가

### 로그인 / 회원가입

수정 파일:

- `src/ui/WorkerCalendarAppFinal.java`

중요 메서드:

- `createLoginPanel()`
- `createSignupPanel()`
- `initializeDefaultUserIfNeeded()`

여기서 할 수 있는 수정:

- 로그인 기본값 변경
- 로그인 실패 메시지 변경
- 회원가입 입력칸 추가
- 테스트 계정 생성 방식 변경

주의:

- 로그인 성공 후 바로 `initializeMainViews()`와 `cardLayout.show(..., "DASHBOARD")`로 넘어갑니다.
- 로그인만 고치고 대시보드 연결을 건드리면 화면 전환이 깨질 수 있습니다.

---

### 메인 화면 형태

수정 파일:

- `src/ui/WorkerCalendarAppFinal.java`
- `src/ui/TopNavigationBar.java`
- `src/ui/SidebarPanel.java`
- `src/ui/CalendarPanel.java`
- `src/ui/DashboardPanel.java`

역할 분리:

- `WorkerCalendarAppFinal.java`
  - 화면들을 어떻게 조립할지 결정
- `TopNavigationBar.java`
  - 상단 메뉴 UI
- `SidebarPanel.java`
  - 왼쪽 근무지 영역 UI
- `CalendarPanel.java`
  - 달력 UI
- `DashboardPanel.java`
  - 급여 카드 / 비율 그래프 UI

화면 배치를 바꾸고 싶으면:

- 먼저 `createDashboardPanel()`을 확인
- 그 다음 실제 모양은 각 분리된 UI 파일에서 수정

---

### 근무지 필터링

수정 파일:

- `src/ui/WorkerCalendarAppFinal.java`
- `src/ui/SidebarPanel.java`
- `src/service/ShiftManager.java`
- `src/service/WorkplaceManager.java`

필터링 흐름:

1. `SidebarPanel`에서 근무지 선택
2. `WorkerCalendarAppFinal.createSidebarPanel()` 안의 `onCategorySelected(...)` 호출
3. 선택한 문자열을 근무지 ID로 변환
4. `selectedWorkplaceFilterId`에 저장
5. `refreshCalendarView()` 실행
6. `getFilteredShifts()`에서 필터 적용
7. `CalendarPanel`이 필터된 일정만 그림

핵심 메서드:

- `WorkerCalendarAppFinal.createSidebarPanel()`
- `WorkerCalendarAppFinal.getFilteredShifts()`
- `WorkerCalendarAppFinal.refreshSidebarWorkplaceList()`
- `ShiftManager.getFilteredShifts(...)`
- `WorkplaceManager.findWorkplaceByDisplayText(...)`

주의:

- 필터가 안 되면 UI 문제가 아니라 "문자열 -> 근무지 ID 매핑" 문제일 가능성이 큽니다.
- `WorkplaceManager.buildWorkplaceDisplayText(...)`와 실제 리스트에 들어가는 문자열 형식이 다르면 필터가 깨집니다.

---

### 일정 등록 / 수정 / 삭제

수정 파일:

- `src/controller/ShiftController.java`
- `src/ui/ShiftEditDialog.java`
- `src/service/ShiftManager.java`

역할 분리:

- `ShiftEditDialog.java`
  - 입력창 UI
- `ShiftController.java`
  - 저장/삭제 버튼 이후 실제 처리
- `ShiftManager.java`
  - Shift 생성, 변환, 필터링 같은 순수 로직

흐름:

1. 달력 날짜 클릭 또는 일정 배지 클릭
2. `WorkerCalendarAppFinal.openShiftEditDialog(...)`
3. `ShiftController.openShiftEditDialog(...)`
4. `ShiftEditDialog` 열림
5. 저장 시 `handleShiftSave(...)`
6. `ShiftManager.createShiftFromUiInput(...)`으로 실제 Shift 생성
7. 저장 후 `refreshCalendarView()` 호출

현재 동작 규칙:

- 날짜 칸 클릭
  - 새 일정 추가 창을 엽니다.
- 일정 배지 클릭
  - 기존 일정 수정/삭제 창을 엽니다.

이 방식으로 같은 날짜에 여러 근무지 일정을 함께 넣을 수 있습니다.

이 기능을 고칠 때 안전한 순서:

1. 입력창 UI만 바꾸고 싶으면 `ShiftEditDialog.java`
2. 저장 조건/검증 바꾸고 싶으면 `ShiftController.java`
3. 근무시간 계산 방식/형식 바꾸고 싶으면 `ShiftManager.java`

---

### 자동 패턴 생성

수정 파일:

- `src/ui/WorkerCalendarAppFinal.java`
- `src/controller/ShiftController.java`

중요 메서드:

- `WorkerCalendarAppFinal.openAutoPatternDialog()`
- `ShiftController.convertPatternToUiShiftTypes(...)`
- `ShiftController.buildShiftForPatternDay(...)`

여기서 할 수 있는 수정:

- 패턴 종류 추가
- 생성 일수 기본값 변경
- 패턴별 기본 시간 변경

패턴을 새로 추가하는 가장 쉬운 방법:

1. `src/ui/WorkerCalendarAppFinal.java`
   `openAutoPatternDialog()` 안의 `patternComboBox` 문자열 배열에 패턴을 추가합니다.
   예: `"주-주-주-휴-휴"`
2. 패턴이 `주/야/비/휴`만 사용하면 1번만 해도 됩니다.
3. 새 토큰을 쓰는 경우
   예: `"오전-오후-휴무"`
   `src/controller/ShiftController.java`의
   `convertPatternToUiShiftTypes(...)`에서 해석 규칙을 추가해야 합니다.
4. 새 토큰에 맞는 기본 시간을 주고 싶으면
   `src/controller/ShiftController.java`의
   `buildShiftForPatternDay(...)`에서 시간 규칙을 추가합니다.

주의:

- 자동 패턴도 결국 `Shift`를 여러 개 만들어 넣는 구조입니다.
- 따라서 마지막에는 반드시 `appendPatternShiftsAndPersist(...)`까지 연결되어야 합니다.

---

### 근무지 추가 / 수정 / 삭제

수정 파일:

- `src/controller/WorkplaceController.java`
- `src/ui/AddWorkplaceDialog.java`
- `src/ui/EditWorkplaceDialog.java`
- `src/ui/DeleteWorkplaceDialog.java`
- `src/service/WorkplaceManager.java`

역할 분리:

- 다이얼로그 파일들
  - 입력 UI
- `WorkplaceController.java`
  - 추가/수정/삭제 흐름
- `WorkplaceManager.java`
  - 근무지 생성, 표시 문자열, 사용 여부 판단

중요 메서드:

- `WorkplaceController.openAddWorkplaceDialog()`
- `WorkplaceController.openEditWorkplaceDialog()`
- `WorkplaceController.openDeleteWorkplaceDialog()`
- `WorkplaceController.buildWorkplaceDisplayList()`
- `WorkplaceManager.buildWorkplaceDisplayText(...)`

주의:

- 근무지 문자열 표시 형식을 바꾸면 필터와 수정/삭제 선택 로직도 같이 영향받을 수 있습니다.

---

### 급여 계산

수정 파일:

- `src/service/SalaryEngine.java`
- `src/service/HourlySalaryStrategy.java`
- `src/service/OffSalaryStrategy.java`
- `src/service/StatisticsService.java`
- `src/ui/DashboardPanel.java`

역할:

- `SalaryEngine`
  - 실제 급여 계산 실행
- `HourlySalaryStrategy`
  - 주간/야간 근무 계산
- `OffSalaryStrategy`
  - 휴무 0원 처리
- `StatisticsService`
  - 합계/기간 통계 계산
- `DashboardPanel`
  - 계산 결과를 화면에 보여줌

화면 숫자만 바꾸고 싶으면:

- `DashboardPanel.java`

계산 규칙 자체를 바꾸고 싶으면:

- `SalaryEngine.java`
- `HourlySalaryStrategy.java`

---

### 마이페이지

수정 파일:

- `src/ui/MyPagePanel.java`
- `src/ui/WorkerCalendarAppFinal.java`

역할:

- `MyPagePanel.java`
  - 입력 UI
- `WorkerCalendarAppFinal.handleMyPageSave(...)`
  - 저장 처리

중요 메서드:

- `createMyPagePanel()`
- `refreshMyPageUserInfo()`
- `handleMyPageSave(...)`

주의:

- 마이페이지 저장은 사용자 정보만 바꾸는 게 아니라 기본 근무지 동기화도 같이 건드립니다.
- 따라서 `syncDefaultWorkplaceFromUserInfo()`까지 같이 봐야 합니다.

---

### 게시판

수정 파일:

- `src/ui/BoardPanel.java`
- `src/ui/WorkerCalendarAppFinal.java`
- `src/model/Post.java`

역할:

- `BoardPanel`
  - 게시글 UI
- `WorkerCalendarAppFinal.createBoardPanel()`
  - 게시글 저장과 화면 연결
- `Post`
  - 게시글 데이터

---

### 파일 저장 / 불러오기

수정 파일:

- `src/ui/WorkerCalendarAppFinal.java`

중요 메서드:

- `loadDataFromFile()`
- `saveDataToFile()`

현재 방식:

- `.dat` 직렬화 파일 저장

주의:

- 이 부분을 잘못 고치면 로그인 정보, 근무지, 일정이 전부 날아갈 수 있습니다.
- 초보 팀원은 이 부분을 함부로 수정하지 않는 것이 좋습니다.

---

## 4. 초보 팀원이 "웬만하면 안 건드리는 게 좋은 곳"

아래는 지금 단계에서 함부로 수정하면 위험한 곳입니다.

- `loadDataFromFile()`
- `saveDataToFile()`
- `initializeControllers()`
- `initializeBackendServices()`
- `ensureUserCollections(...)`
- `mergeShiftWorkplacesIntoUser(...)`

이유:

- 프로그램 시작 흐름과 데이터 복구 흐름이 들어 있음
- 잘못 건드리면 로그인, 필터, 저장 기능이 같이 망가질 수 있음

---

## 5. 수정할 때 가장 안전한 방법

### UI 모양만 바꾸고 싶을 때

- `ui` 폴더 안의 해당 패널만 수정
- `service`, `controller`는 건드리지 않기

예:

- 버튼 색 변경
- 라벨 문구 변경
- 패널 크기 변경

---

### 버튼 클릭 후 동작을 바꾸고 싶을 때

- 먼저 `ui`에서 어떤 컨트롤러를 호출하는지 확인
- 실제 동작은 `controller`에서 수정

예:

- 일정 저장 검증 바꾸기
- 근무지 삭제 조건 바꾸기

---

### 계산 규칙을 바꾸고 싶을 때

- `service`만 수정
- `ui`는 최대한 건드리지 않기

예:

- 야간수당 규칙 추가
- 세후 급여 계산 변경

---

## 6. 기능이 고장났을 때 확인 순서

### 1. 버튼은 눌리는데 아무 일도 안 일어남

확인:

- `ui`에서 이벤트가 연결되어 있는가
- 해당 컨트롤러 메서드가 호출되는가

먼저 볼 곳:

- `WorkerCalendarAppFinal.java`
- `ShiftController.java`
- `WorkplaceController.java`

---

### 2. 목록은 보이는데 필터가 안 먹음

확인:

- `selectedWorkplaceFilterId`에 값이 들어가는가
- `WorkplaceManager.findWorkplaceByDisplayText(...)`가 제대로 찾는가
- `ShiftManager.getFilteredShifts(...)`가 실제로 걸러주는가

---

### 3. 달력은 바뀌는데 급여가 안 맞음

확인:

- `refreshCalendarView()` 호출 후 `refreshDashboardPanel()`도 호출되는가
- `StatisticsService` 계산값이 정상인가

---

### 4. 저장 후 다시 실행하면 데이터가 사라짐

확인:

- `saveDataToFile()`이 호출됐는가
- 직렬화 대상 객체에 값이 정상적으로 들어 있는가

---

## 7. 팀원별 추천 담당 분리

초보 팀원 기준으로는 아래처럼 나누는 것이 안전합니다.

### UI 담당

수정 추천 파일:

- `TopNavigationBar.java`
- `SidebarPanel.java`
- `CalendarPanel.java`
- `MyPagePanel.java`
- `BoardPanel.java`

이유:

- 비교적 화면만 다루므로 사고 범위가 작음

---

### 일정 기능 담당

수정 추천 파일:

- `ShiftEditDialog.java`
- `ShiftController.java`
- `ShiftManager.java`

이유:

- 일정 등록/수정 로직만 집중해서 다룰 수 있음

---

### 근무지 기능 담당

수정 추천 파일:

- `AddWorkplaceDialog.java`
- `EditWorkplaceDialog.java`
- `DeleteWorkplaceDialog.java`
- `WorkplaceController.java`
- `WorkplaceManager.java`

---

### 계산/통계 담당

수정 추천 파일:

- `SalaryEngine.java`
- `HourlySalaryStrategy.java`
- `StatisticsService.java`
- `DashboardPanel.java`

---

## 8. 지금 프로젝트에서 가장 자주 건드리는 지점

실제로 수정 요청이 많이 들어올 가능성이 큰 곳은 아래입니다.

- 달력에 일정 표시 형식 변경
- 근무지 필터링
- 일정 등록/수정 창
- 급여 카드 숫자 표시
- 마이페이지 입력값 저장

즉, 초보 팀원이 가장 먼저 익숙해져야 할 파일은 이 다섯 개입니다.

- `src/ui/WorkerCalendarAppFinal.java`
- `src/ui/CalendarPanel.java`
- `src/ui/SidebarPanel.java`
- `src/controller/ShiftController.java`
- `src/controller/WorkplaceController.java`

---

## 9. 마지막 팁

이 프로젝트는 아직 `WorkerCalendarAppFinal.java`에 기능이 많이 몰려 있습니다.

그래서 수정할 때는 항상 아래 원칙을 지키는 것이 좋습니다.

- 화면 모양 수정은 `ui`
- 저장/삭제 흐름 수정은 `controller`
- 계산/변환 수정은 `service`
- 데이터 구조 수정은 `model`

이 원칙만 지켜도 "어디를 고쳐야 할지 몰라서 여러 파일을 동시에 건드리는 문제"를 많이 줄일 수 있습니다.

---

## 10. 추천 읽기 순서

처음 보는 팀원은 아래 순서로 읽는 것을 추천합니다.

1. `src/ui/WorkerCalendarAppFinal.java`
2. `src/ui/CalendarPanel.java`
3. `src/ui/SidebarPanel.java`
4. `src/controller/ShiftController.java`
5. `src/controller/WorkplaceController.java`
6. `src/service/ShiftManager.java`
7. `src/service/WorkplaceManager.java`

이 순서로 보면 화면 -> 버튼 동작 -> 실제 로직 순서로 이해할 수 있습니다.

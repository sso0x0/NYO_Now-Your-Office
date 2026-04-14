import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import model.Shift;
import model.Workplace;

public class InspectData {
  public static void main(String[] args) throws Exception {
    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("nyo_database_v2.dat"))) {
      Object data = ois.readObject();
      Class<?> dataClass = data.getClass();
      Field userDbField = dataClass.getDeclaredField("userDB");
      userDbField.setAccessible(true);
      Map<?,?> userDb = (Map<?,?>) userDbField.get(data);
      System.out.println("users=" + userDb.keySet());
      for (Map.Entry<?,?> e : userDb.entrySet()) {
        Object user = e.getValue();
        Class<?> uc = user.getClass();
        Field nameF = uc.getDeclaredField("name"); nameF.setAccessible(true);
        Field workplacesF = uc.getDeclaredField("workplaces"); workplacesF.setAccessible(true);
        Field shiftsF = uc.getDeclaredField("shifts"); shiftsF.setAccessible(true);
        System.out.println("user=" + e.getKey() + ", name=" + nameF.get(user));
        List<Workplace> workplaces = (List<Workplace>) workplacesF.get(user);
        List<Shift> shifts = (List<Shift>) shiftsF.get(user);
        System.out.println("workplaces:");
        if (workplaces != null) for (Workplace w : workplaces) {
          System.out.println("  WP id=" + w.getId() + ", name=" + w.getCompanyName() + ", rate=" + w.getHourlyRate());
        }
        System.out.println("shifts:");
        if (shifts != null) for (Shift s : shifts) {
          Workplace w = s.getWorkplace();
          System.out.println("  Shift id=" + s.getId() + ", date=" + s.getStartTime().toLocalDate() + ", wpId=" + (w==null?"null":w.getId()) + ", wpName=" + (w==null?"null":w.getCompanyName()));
        }
      }
    }
  }
}
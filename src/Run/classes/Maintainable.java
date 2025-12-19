package Run.classes;

import java.util.Date;

public interface Maintainable {
    void scheduleMaintenance(Date date);
    boolean isUnderMaintenance();
    Date getMaintenanceDate();
}

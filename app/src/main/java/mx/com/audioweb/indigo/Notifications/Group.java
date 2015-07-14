package mx.com.audioweb.indigo.Notifications;

/**
 * Created by Juan Acosta on 1/28/2015.
 */
public class Group {
    private String group_name;
    private String group_contacts;
    private String group_organizer;
    private String group_id;

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public String getGroup_contacts() {
        return group_contacts;
    }

    public void setGroup_contacts(String group_contacts) {
        this.group_contacts = group_contacts;
    }

    public String getGroup_organizer() {
        return group_organizer;
    }

    public void setGroup_organizer(String group_organizer) {
        this.group_organizer = group_organizer;
    }
}

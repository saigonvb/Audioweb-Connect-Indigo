package mx.com.audioweb.indigo;

/**
 * Created by Juan Acosta on 10/22/2014.
 */
public class User_info {
    private String id;
    private String smen_id;
    private String ac_org_code;
    private String ac_part_code;
    private String ac_usuario;
    private String COS;
    public static String USER_ID;

    public String getId() {

        USER_ID = id;
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSmen_id() {
        return smen_id;
    }

    public void setSmen_id(String smen_id) {
        this.smen_id = smen_id;
    }

    public String getAc_org_code() {
        return ac_org_code;
    }

    public void setAc_org_code(String ac_org_code) {
        this.ac_org_code = ac_org_code;
    }

    public String getAc_part_code() {
        return ac_part_code;
    }

    public void setAc_part_code(String ac_part_code) {
        this.ac_part_code = ac_part_code;
    }

    public String getAc_usuario() {
        return ac_usuario;
    }

    public void setAc_usuario(String ac_usuario) {
        this.ac_usuario = ac_usuario;
    }

    public String getCOS() {
        return COS;
    }

    public void setCOS(String COS) {
        this.COS = COS;
    }


}

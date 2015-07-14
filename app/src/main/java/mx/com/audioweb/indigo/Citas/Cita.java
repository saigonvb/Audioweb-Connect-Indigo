package mx.com.audioweb.indigo.Citas;

import java.io.Serializable;

/**
 * Created by Juan Acosta on 11/10/2014.
 */
public class Cita implements Serializable{
    private String id;
    private String empresa;
    private String fechaInicio;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }
}

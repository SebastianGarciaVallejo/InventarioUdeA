package co.edu.udea.modelo.listado;

import com.google.gson.annotations.SerializedName;

public class Articulo {
    private String id;
    @SerializedName("name")
    private String nombres;
    @SerializedName("image_url")
    private String imagen;
    @SerializedName("description")
    private String descripcion;
    @SerializedName("updated_at")
    private String ultimaActualizacion;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(String ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }

}

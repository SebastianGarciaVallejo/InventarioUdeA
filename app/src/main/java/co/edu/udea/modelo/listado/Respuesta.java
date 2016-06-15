package co.edu.udea.modelo.listado;

import java.util.ArrayList;

public class Respuesta {
    private String status;
    private String error;
    private ArrayList<Articulo> data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public ArrayList<Articulo> getData() {
        return data;
    }

    public void setData(ArrayList<Articulo> data) {
        this.data = data;
    }
}
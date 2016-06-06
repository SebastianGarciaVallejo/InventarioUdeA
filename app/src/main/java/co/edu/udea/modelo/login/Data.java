package co.edu.udea.modelo.login;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Data {
    private String id;
    private String token;
    @SerializedName("laboratories")
    private ArrayList<Laboratorio> listaLaboratorios;

    public Data()
    {
        listaLaboratorios = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public ArrayList<Laboratorio> getListaLaboratorios() {
        return listaLaboratorios;
    }

    public void setListaLaboratorios(ArrayList<Laboratorio> listaLaboratorios) {
        this.listaLaboratorios = listaLaboratorios;
    }
}

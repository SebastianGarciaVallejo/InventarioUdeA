package co.edu.udea.modelo.utiles;


import java.io.Serializable;

public class IntermediarioActividades implements Serializable{

    private static Object objeto;

    public static Object getObjetoATransmitirEntreActividades() {
        return objeto;
    }

    public static void setObjetoATransmitirEntreActividades(Object objetoATransmitirEntreActividades) {
        objeto = objetoATransmitirEntreActividades;
    }
}

package main.java.es.uclm.sri.jsrilanda.beans;

import java.io.Serializable;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import main.java.es.uclm.sri.jsrilanda.logica.RecomendacionesByLastfm;
import main.java.es.uclm.sri.sis.entidades.Recomendacion;

@ManagedBean(name = "recomendacionesBean")
@ApplicationScoped
public class RecomendacionBean implements Serializable {
    
    private String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public void analizarUserLastfm() {
        
    }
    
    public void generarRecomendaciones() {
        Recomendacion[] recomendaciones = RecomendacionesByLastfm.generarRecomendacionesByLastfm(getUserName());
    }

}

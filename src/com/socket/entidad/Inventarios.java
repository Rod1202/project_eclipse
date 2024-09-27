package com.socket.entidad;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "inventarios")
public class Inventarios {
    private List<Inventario> inventarios;

    @XmlElement(name = "inventario") // Asegúrate de que este nombre coincide con el XML
    public List<Inventario> getInventarios() {
        return inventarios;
    }

    public void setInventarios(List<Inventario> inventarios) {
        this.inventarios = inventarios;
    }
}

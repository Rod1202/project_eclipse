package com.socket.entidad;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "productos")
public class Productos {
    private List<Producto> producto;

    public List<Producto> getProducto() {
        return producto;
    }

    @XmlElement(name = "producto")
    public void setProducto(List<Producto> producto) {
        this.producto = producto;
    }
}

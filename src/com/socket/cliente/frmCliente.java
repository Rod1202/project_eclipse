package com.socket.cliente;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.google.gson.Gson;
import com.socket.entidad.Inventario;
import com.socket.entidad.Producto;
import com.socket.entidad.Productos;

import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionEvent;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JTextField;

public class frmCliente extends JFrame implements ActionListener {
    
    private JPanel contentPane;
    private JButton btnEnviar;
    private JButton btnCierre;
    private JTable tblProductos;  // Cambié el nombre para reflejar que es una tabla de productos
    private JButton btnTraer;
    private JButton btnEliminar;
    
    private List<Producto> listaProductos; // Lista para almacenar productos cargados
    private boolean inventarioEnviado = false; // Bandera para evitar envíos múltiples

    // Lanza la aplicación.
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    frmCliente frame = new frmCliente();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Crea el marco.
    public frmCliente() {
        setTitle("Cliente - Inventario");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 553, 368);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        btnEnviar = new JButton("");
        btnEnviar.setIcon(new ImageIcon(frmCliente.class.getResource("/img/charlar.png")));
        btnEnviar.addActionListener(this);
        btnEnviar.setBounds(77, 11, 57, 46);
        contentPane.add(btnEnviar);

        btnCierre = new JButton("");
        btnCierre.setIcon(new ImageIcon(frmCliente.class.getResource("/img/undo.png")));
        btnCierre.addActionListener(this);
        btnCierre.setBounds(144, 11, 57, 46);
        contentPane.add(btnCierre);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(10, 98, 523, 220);
        contentPane.add(scrollPane);

        tblProductos = new JTable();
        tblProductos.setModel(new DefaultTableModel(
            new Object[][] {},
            new String[] { "CÓDIGO", "NOMBRE", "PRECIO", "STOCK" } // Cambié los nombres de columnas para reflejar productos
        ));
        tblProductos.getColumnModel().getColumn(0).setPreferredWidth(63);
        tblProductos.getColumnModel().getColumn(1).setPreferredWidth(143);
        tblProductos.getColumnModel().getColumn(2).setPreferredWidth(138);
        tblProductos.setFillsViewportHeight(true);
        scrollPane.setViewportView(tblProductos);

        btnTraer = new JButton("");
        btnTraer.setIcon(new ImageIcon(frmCliente.class.getResource("/img/inventario.png")));
        btnTraer.addActionListener(this);
        btnTraer.setBounds(10, 11, 57, 46);
        contentPane.add(btnTraer);

        btnEliminar = new JButton("");
        btnEliminar.addActionListener(this);
        btnEliminar.setIcon(new ImageIcon(frmCliente.class.getResource("/img/borrar.png")));
        btnEliminar.setBounds(482, 64, 51, 33);
        contentPane.add(btnEliminar);
    }

    public void actionPerformed(final ActionEvent arg0) {
        if (arg0.getSource() == btnEliminar) {
            actionPerformedBtnEliminar(arg0);
        }
        if (arg0.getSource() == btnTraer) {
            actionPerformedBtnTraer(arg0);
        }
        if (arg0.getSource() == btnCierre) {
            actionPerformedBtnCierre(arg0);
        }
        if (arg0.getSource() == btnEnviar) {
            actionPerformedBtnEnviar(arg0);
        }
    }

    protected void actionPerformedBtnEnviar(final ActionEvent arg0) {
        if (inventarioEnviado) {
            System.out.println("El inventario ya ha sido enviado.");
            return; // Evita enviar el inventario más de una vez.
        }

        try {
            // Crea un socket para conectarse al servidor
            Socket socket = new Socket("localhost", 12345); // Cambia el puerto según sea necesario
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());

            // Crea el objeto de inventario y llena con la lista de productos
            Inventario inventario = new Inventario();
            inventario.setCodigo("INV001"); // Puedes cambiar esto como desees
            inventario.setFecha(LocalDate.now().toString());
            inventario.setProductos(listaProductos); // Asignar la lista de productos

            // Envía el inventario al servidor
            outputStream.writeObject(inventario);
            outputStream.flush();

            System.out.println("Inventario enviado: " + inventario.getCodigo());
            inventarioEnviado = true; // Cambia la bandera a true para evitar envíos múltiples

            outputStream.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void actionPerformedBtnCierre(final ActionEvent arg0) {
        System.exit(0);
    }

    protected void actionPerformedBtnTraer(final ActionEvent arg0) {
        cargarProductosDesdeXML(); // Cargar productos desde XML
        cargarProductosDesdeJSON(); // Cargar productos desde JSON
        actualizarTablaProductos();
    }

    protected void actionPerformedBtnEliminar(final ActionEvent arg0) {
        int selectedRow = tblProductos.getSelectedRow();
        if (selectedRow != -1) {
            DefaultTableModel model = (DefaultTableModel) tblProductos.getModel();
            model.removeRow(selectedRow);
            listaProductos.remove(selectedRow); // Eliminar de la lista también
        }
    }

    private void cargarProductosDesdeXML() {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Productos.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Productos productos = (Productos) unmarshaller.unmarshal(new File("D:\\ServiciosWeb_Archivos\\DSWII-Modelo\\DSWII-Modelo\\productos.xml")); // Cambia la ruta al XML

            listaProductos = productos.getProducto(); // Obtener lista de productos
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cargarProductosDesdeJSON() {
        try {
            Gson gson = new Gson();
            FileReader reader = new FileReader("D:\\ServiciosWeb_Archivos\\DSWII-Modelo\\DSWII-Modelo\\productos.json"); // Cambia la ruta al JSON
            Producto[] productos = gson.fromJson(reader, Producto[].class);
            if (listaProductos == null) {
                listaProductos = new ArrayList<>();
            }
            for (Producto producto : productos) {
                listaProductos.add(producto);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void actualizarTablaProductos() {
        DefaultTableModel model = (DefaultTableModel) tblProductos.getModel();
        model.setRowCount(0); // Limpiar la tabla antes de agregar
        for (Producto producto : listaProductos) {
            model.addRow(new Object[] { producto.getCodigo(), producto.getNombre(), producto.getPrecio(), producto.getStock() });
        }
    }
}

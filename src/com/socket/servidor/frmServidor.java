package com.socket.servidor;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import com.socket.entidad.Inventario;
import com.socket.entidad.Inventarios;
import com.socket.entidad.Producto;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.FileReader;

public class frmServidor extends JFrame implements ActionListener, Runnable {

    private JPanel contentPane;
    private JTextArea txtS;
    private JScrollPane scrollPane;
    private JButton btnCierre;
    private JTable tblProductos;
    private JLabel lblTotales;
    private JTextField txtCodigoInventario;
    private JLabel lblFechaSistema;
    private Map<String, Inventario> inventarios; // Mapa para guardar los inventarios cargados

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    frmServidor frame = new frmServidor();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public frmServidor() {
        setTitle("Servidor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 600, 500);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

       
        JLabel lblCodigoInventario = new JLabel("Código de Inventario:");
        lblCodigoInventario.setFont(new Font("Tahoma", Font.PLAIN, 12));
        lblCodigoInventario.setBounds(23, 20, 140, 14);
        contentPane.add(lblCodigoInventario);

        txtCodigoInventario = new JTextField();
        txtCodigoInventario.setBounds(160, 17, 120, 20);
        contentPane.add(txtCodigoInventario);
        txtCodigoInventario.setColumns(10);

       
        JButton btnBuscarInventario = new JButton("Buscar");
        btnBuscarInventario.setFont(new Font("Tahoma", Font.PLAIN, 12));
        btnBuscarInventario.setBounds(290, 16, 80, 23);
        contentPane.add(btnBuscarInventario);
        btnBuscarInventario.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buscarInventarioPorCodigo();
            }
        });

       
        lblFechaSistema = new JLabel("Fecha: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lblFechaSistema.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblFechaSistema.setBounds(400, 20, 180, 14);
        contentPane.add(lblFechaSistema);

    
        JScrollPane scrollPane_1 = new JScrollPane();
        scrollPane_1.setBounds(23, 60, 523, 200);
        contentPane.add(scrollPane_1);

        tblProductos = new JTable();
        tblProductos.setModel(new DefaultTableModel(
            new Object[][] {},
            new String[] { "CÓDIGO", "NOMBRE", "PRECIO", "STOCK" }
        ));
        tblProductos.getColumnModel().getColumn(1).setPreferredWidth(131);
        tblProductos.getColumnModel().getColumn(2).setPreferredWidth(147);
        tblProductos.getColumnModel().getColumn(3).setPreferredWidth(119);
        tblProductos.setFillsViewportHeight(true);
        scrollPane_1.setViewportView(tblProductos);

     
        lblTotales = new JLabel("Totales:");
        lblTotales.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblTotales.setBounds(23, 270, 264, 14);
        contentPane.add(lblTotales);

     
        scrollPane = new JScrollPane();
        scrollPane.setBounds(23, 300, 523, 107);
        contentPane.add(scrollPane);

        txtS = new JTextArea();
        txtS.setEditable(false);
        scrollPane.setViewportView(txtS);

       
        btnCierre = new JButton("Cerrar");
        btnCierre.setFont(new Font("Tahoma", Font.PLAIN, 12));
        btnCierre.setBounds(485, 10, 80, 30);
        contentPane.add(btnCierre);
        btnCierre.addActionListener(this);

       
        inventarios = new HashMap<>();
        cargarInventarios();

       
        Thread h = new Thread(this);
        h.start();
    }

    
    private void buscarInventarioPorCodigo() {
        String codigo = txtCodigoInventario.getText().trim();

        if (codigo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese un código de inventario.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Inventario inventario = inventarios.get(codigo);
        if (inventario != null) {
            if (inventario.getProductos() != null && !inventario.getProductos().isEmpty()) {
                listado(inventario.getProductos());
                lblTotales.setText("Totales: Productos: " + tblProductos.getRowCount() + ", Stock Total: " + calcularStockTotal(inventario.getProductos()));
            } else {
                JOptionPane.showMessageDialog(this, "No hay productos en este inventario.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Inventario no encontrado.", "Error", JOptionPane.ERROR_MESSAGE);
            limpiarTabla();
            lblTotales.setText("Totales:");
        }
    }



   
    private double calcularStockTotal(List<Producto> lista) {
        double total = 0;
        for (Producto prod : lista) {
            total += prod.getStock();
        }
        return total;
    }

  
    private void limpiarTabla() {
        DefaultTableModel modelo = (DefaultTableModel) tblProductos.getModel();
        modelo.setRowCount(0);
    }

    private void cargarInventarios() {
        try {
            String ruta = System.getProperty("user.dir");
            JAXBContext context = JAXBContext.newInstance(Inventarios.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            
            Inventarios inventarios = (Inventarios) unmarshaller.unmarshal(new FileReader(ruta + "/productos.xml"));
            
            for (Inventario inventario : inventarios.getInventarios()) {
                this.inventarios.put(inventario.getCodigo(), inventario);
                System.out.println("Cargado inventario: " + inventario.getCodigo()); 
                for (Producto producto : inventario.getProductos()) {
                	System.out.println("Producto: " + producto.getCodigo() + ", Nombre: " + producto.getNombre() +
                            ", Precio: " + producto.getPrecio() + ", Stock: " + producto.getStock());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



  
    private void listado(List<Producto> productos) {
        DefaultTableModel modelo = (DefaultTableModel) tblProductos.getModel();
        modelo.setRowCount(0);
        for (Producto producto : productos) {
            Object[] row = {producto.getCodigo(), producto.getNombre(), producto.getPrecio(), producto.getStock()};
            modelo.addRow(row);
        }
    }


    @Override
    public void run() {
        try (ServerSocket server = new ServerSocket(1026)) {
            txtS.append("Servidor iniciado y escuchando en el puerto 1026.\n");
            while (true) {
                try (Socket cliente = server.accept();
                     ObjectInputStream stream = new ObjectInputStream(cliente.getInputStream())) {

                    List<Producto> data = (List<Producto>) stream.readObject();
                    listado(data);
                    txtS.append("Datos recibidos y tabla actualizada.\n");
                } catch (Exception e) {
                    e.printStackTrace();
                    txtS.append("Error al procesar la conexión: " + e.getMessage() + "\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            txtS.append("Error al iniciar el servidor: " + e.getMessage() + "\n");
        }
    }

   
    public void actionPerformed(ActionEvent arg0) {
        if (arg0.getSource() == btnCierre) {
            System.exit(0);
        }
    }
}

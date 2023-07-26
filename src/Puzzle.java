import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Puzzle extends JFrame {
    private JPanel panel;
    private BufferedImage origen;
    private BufferedImage medida;
    private Image image;
    private Boton ultimoBoton;
    private int ancho, alto;
    private List<Boton> botones;
    private List<Point> solucion;
    private final int NUMERO_DE_BOTONES = 12;

    // la imagen utilizada se re-escala a este ancho
    private final int ANCHO_DESEADO = 600;
    private int movimientos = 0;

    public Puzzle(){
        iniciarUI();
    }

    private void iniciarUI() {
        // la lista "solucion" guarda el orden correcto de los botones
        solucion = new ArrayList<>();

        // cada boton se identifica con un punto
        solucion.add(new Point(0,0));
        solucion.add(new Point(0,1));
        solucion.add(new Point(0,2));
        solucion.add(new Point(1,0));
        solucion.add(new Point(1,1));
        solucion.add(new Point(1, 2));
        solucion.add(new Point(2, 0));
        solucion.add(new Point(2, 1));
        solucion.add(new Point(2, 2));
        solucion.add(new Point(3, 0));
        solucion.add(new Point(3, 1));
        solucion.add(new Point(3, 2));

        botones = new ArrayList<>();

        panel = new JPanel();
        panel.setBorder(BorderFactory.createLineBorder(Color.gray));

        // se utiliza un GridLayout para almacenar los botones
        // estableciendo un diseño de 4 filas y 3 columnas
        panel.setLayout(new GridLayout(4,3,0,0));

        try{
            origen = cargarImagen();
            int h = getNuevaAltura(origen.getWidth(), origen.getHeight());
            medida = cambiarMedida(origen, ANCHO_DESEADO, h,
                    BufferedImage.TYPE_INT_ARGB);
        } catch (IOException ex){
            Logger.getLogger(Puzzle.class.getName()).log(
                    Level.SEVERE, null, ex);
        }

        ancho = medida.getWidth();
        alto = medida.getHeight();

        add(panel, BorderLayout.CENTER);

        for (int i = 0; i < 4; i++){
            for (int j = 0; j < 3; j++){
                // CropImageFilter corta una region rectangular de la imagen original
                // utiliza FilteredImageSource para crear una imagen recortada
                image = createImage(new FilteredImageSource(medida.getSource(),
                        new CropImageFilter(j * ancho / 3, i * alto / 4,
                                (ancho / 3), alto / 4)));

                Boton boton = new Boton(image);
                // los botones se identifican por su propiedad "posicion"
                // el cual es su posicion correcta en el tablero
                boton.putClientProperty("posicion", new Point(i,j));

                if(i == 3 && j == 2){
                    /* el ultimo boton es el unico cuadro vacio en el tablero,
                        el cual permite cambiar su pocicion con los botones adyacentes.
                        Se coloca al final de tablero, esquina inferior derecha.
                     */
                    ultimoBoton = new Boton();
                    ultimoBoton.setBorderPainted(false);
                    ultimoBoton.setContentAreaFilled(false);
                    ultimoBoton.setUltimo();
                    ultimoBoton.putClientProperty("posicion", new Point(i, j));
                } else{
                    botones.add(boton);
                }
            }
        }

        // los botones se reordenan aleatoriamente
        Collections.shuffle(botones);
        // el ultimo boton se ingresa al final.
        botones.add(ultimoBoton);

        // todos los elementos de la lista de botones se colocan en el panel
        // se les asigna un borde de color gris
        // y un  click action listener
        for (int i = 0; i < NUMERO_DE_BOTONES; i++){
            Boton btn = botones.get(i);
            panel.add(btn);
            btn.setBorder(BorderFactory.createLineBorder(Color.gray));
            btn.addActionListener(new ClickAction());
        }

        pack();
        setTitle("Puzzle");
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // la imagen original se redimensiona para crear una imagen con nuevas dimensiones
    // la imagen nueva se almacena en buffer y se dibuja en base a la imagen original
    private BufferedImage cambiarMedida(BufferedImage original, int ancho,
                                        int alto, int tipo) throws IOException{
        BufferedImage  nuevaMedida= new BufferedImage(ancho, alto, tipo);
        Graphics2D g = nuevaMedida.createGraphics();
        g.drawImage(original, 0, 0, ancho, alto, null);
        g.dispose();

        return nuevaMedida;
    }

    // se calcula la altura de la imagen manteniendo la proporcion en base al ancho deseado
    // esto permite escalar la imagen a un nuevp tamaño
    private int getNuevaAltura(int anchura, int altura) {
        double radio = ANCHO_DESEADO / (double) anchura;
        int nuevaAltura = (int) (altura * radio);
        return nuevaAltura;
    }

    // carga una imagen desde el disco, devuelve un BufferedImage
    private BufferedImage cargarImagen() throws IOException {
        BufferedImage img = ImageIO.read(new File("resource/rem.jpg"));
        return img;
    }

    private class ClickAction extends AbstractAction{

        @Override
        public void actionPerformed(ActionEvent e) {
            movimientos++;
            checkBoton(e);
            checkSolucion();
        }

        /* Los botones se almacenan en un array list,la cual se asigna
           a la cuadricula del panel.
           Se obtienen los indices del ultimo boton y del boton que se
           le hace click, si son adyacentes, estos cambian de posicion.
         */
        private void checkBoton (ActionEvent e){
            int lidx = 0;

            for (Boton boton : botones){
                if (boton.isUltimo()){
                    lidx = botones.indexOf(boton);
                }
            }
            JButton jboton = (JButton) e.getSource();
            int bidx = botones.indexOf(jboton);

            if ((bidx - 1 == lidx) || (bidx + 1 == lidx)
            || (bidx - 3 == lidx) || (bidx + 3 == lidx)){
                Collections.swap(botones, bidx, lidx);
                actualizarBotones();
            }
        }
        /* asigna la lista de botones a la cuadricula del panel,
           primero elimina todos los elementos y utiliza un bucle
           para recorrer la lista de botones y volver agregar los botones
           reordenados, al final implementa un nuevo layout.
         */
        private void actualizarBotones(){
            panel.removeAll();
            for(JComponent btn : botones){
                panel.add(btn);
            }
            panel.validate();
        }
    }
    /* este metodo compara los puntos actuales de los botones,
        con los puntos ordenados correctamente, si coinciden muestra un
        mensaje anunciando el fin del juego.
     */
    private void checkSolucion(){
        List<Point> lista = new ArrayList<>();

        for (JComponent btn : botones) {
            lista.add((Point) btn.getClientProperty("posicion"));
        }

        if (compararLista(solucion, lista)) {
            JOptionPane.showMessageDialog(panel, "¡Ganaste! \n Numero de movimientos: "+movimientos,
                    "Juego Terminado", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    public static boolean compararLista (List<Point> lista1, List<Point> lista2){
        if (lista1.size() != lista2.size()) {
            return false;
        }
        for (int i = 0; i < lista1.size(); i++) {
            Point p1 = lista1.get(i);
            Point p2 = lista2.get(i);
            if (!p1.equals(p2)) {
                return false;
            }
        }
        return true;
    }
    public static void main (String[] args){
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                Puzzle game = new Puzzle();
                game.setVisible(true);
            }
        });
    }
}

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// la clase boton es el componente que hara de cuadro en el puzzle
public class Boton extends JButton {
    // el ultimo boton es aquel que no tiene imagen
    private boolean esUltimo;
    public Boton (){
        super();
        iniciarUI();
    }
    public Boton (Image image){
        super(new ImageIcon(image));
        iniciarUI();
    }
    private void iniciarUI() {
        esUltimo = false;
        BorderFactory.createLineBorder(Color.gray);

        addMouseListener(new MouseAdapter() {
            // cuando el puntero esta sobre un cuadro, el color del borde cambia a amarillo
            @Override
            public void mouseEntered(MouseEvent e) {
                setBorder(BorderFactory.createLineBorder(Color.yellow));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBorder(BorderFactory.createLineBorder(Color.gray));
            }
        });
    }
    public void setUltimo(){
        esUltimo = true;
    }
    public boolean isUltimo(){
        return esUltimo;
    }
}

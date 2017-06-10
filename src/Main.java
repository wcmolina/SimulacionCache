import javax.swing.JOptionPane;

public class Main {

    public static void main(String[] args) {
        String input = (String)JOptionPane.showInputDialog(
                null, "Ingrese N (direcciones)\nPor defecto N = 4096", "Simulación de cache", JOptionPane.PLAIN_MESSAGE, null, null, "4096");

        if (input != null) {
            try {
                int direcciones;
                direcciones = (input.isEmpty()) ? 4096 : Integer.parseInt(input);
                if (direcciones < 5 || direcciones > 4096) {
                    JOptionPane.showMessageDialog(null, "Error: número inválido", "Error", JOptionPane.ERROR_MESSAGE);
                }
                Simulacion simulacion = new Simulacion(direcciones);
                simulacion.correr();

                JOptionPane.showMessageDialog(null, simulacion.resultados, "Resultados", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(null, "Error: número inválido", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

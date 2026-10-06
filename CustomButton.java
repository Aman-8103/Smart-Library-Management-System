import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CustomButton extends JButton {
    private Color backgroundColor;
    private boolean isLink;
    private int radius = 10;

    public CustomButton(String text, Color backgroundColor, boolean isLink) {
        super(text);
        this.backgroundColor = backgroundColor;
        this.isLink = isLink;
        setupButton();
    }

    private void setupButton() {
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(250, 40));

        if (isLink) {
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            setForeground(backgroundColor);
        } else {
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(Color.WHITE);
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isLink) {
                    setForeground(backgroundColor.darker());
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (isLink) {
                    setForeground(backgroundColor);
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (!isLink) {
            if (getModel().isPressed()) {
                g2d.setColor(backgroundColor.darker().darker());
            } else if (getModel().isRollover()) {
                g2d.setColor(backgroundColor.darker());
            } else {
                g2d.setColor(backgroundColor);
            }
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        }

        super.paintComponent(g);
    }
}

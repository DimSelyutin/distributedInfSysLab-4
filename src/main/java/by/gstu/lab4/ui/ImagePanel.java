package by.gstu.lab4.ui;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class ImagePanel extends JPanel {

    private BufferedImage image;

    public ImagePanel() {
        setPreferredSize(new Dimension(420, 360));
        setBackground(new Color(245, 245, 245));
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        repaint();
    }

    public BufferedImage getImage() {
        return image;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image == null) {
            g.setColor(Color.DARK_GRAY);
            g.drawString("Нет изображения", 20, 30);
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        double scale = Math.min(
                (double) getWidth() / image.getWidth(),
                (double) getHeight() / image.getHeight());
        int w = (int) Math.round(image.getWidth() * scale);
        int h = (int) Math.round(image.getHeight() * scale);
        int x = (getWidth() - w) / 2;
        int y = (getHeight() - h) / 2;
        g2.drawImage(image, x, y, w, h, null);
        g2.dispose();
    }
}

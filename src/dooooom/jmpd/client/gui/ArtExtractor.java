package dooooom.jmpd.client.gui;

import com.mpatric.mp3agic.*;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;

public class ArtExtractor {


    public ArtExtractor() {

    }

    public String extract(String path) {
        String art = null;
        try {
            Mp3File mp3 = new Mp3File(path);
            ID3v2 tag = mp3.getId3v2Tag();
            if (tag != null) {
                String mimeType = tag.getAlbumImageMimeType();
                byte[] rawImage = tag.getAlbumImage();
                String filename = null;

                if(mimeType.equals("image/png")) {
                    filename = "cover.png";
                } else if(mimeType.equals("image/jpg")) {
                    filename = "cover.jpg";
                }
                if(filename != null) {
                    OutputStream out = new BufferedOutputStream(new FileOutputStream(filename));
                    out.write(rawImage);
                    out.close();
                    art = filename;
                }
            }
        } catch (IOException e) {
            //Something went wrong loading the file
        } catch (Exception e) {

        }
        return art;
    }

    public static javafx.scene.image.Image createImage(java.awt.Image image) throws IOException {
        if (!(image instanceof RenderedImage)) {
            BufferedImage bufferedImage = new BufferedImage(image.getWidth(null),
                    image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics g = bufferedImage.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();

            image = bufferedImage;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write((RenderedImage) image, "png", out);
        out.flush();
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        return new javafx.scene.image.Image(in);
    }
}

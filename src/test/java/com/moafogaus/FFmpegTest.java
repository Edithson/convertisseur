package com.moafogaus;

import com.moafogaus.model.MediaItem;
import com.moafogaus.model.TargetFormat;
import com.moafogaus.service.FFmpegService;
import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.EncodingAttributes;

import java.io.File;

public class FFmpegTest {
    public static void main(String[] args) {
        File source = new File("/tmp/test_source.wav");
        FFmpegService service = FFmpegService.getInstance();
        Encoder encoder = new Encoder();

        System.out.println("=== DEBUT DE TEST DE CONVERSION DES FORMATS ===");

        for (TargetFormat format : TargetFormat.values()) {
            try {
                MediaItem item = new MediaItem(source);
                item.setTargetFormat(format);

                File outputDir = new File("/tmp/ffmpeg_test_out");
                outputDir.mkdirs();

                File outputFile = service.determineOutputFile(item, outputDir);
                if (outputFile.exists()) outputFile.delete();

                EncodingAttributes attrs = service.buildEncodingAttributes(item);

                System.out.print("Testing " + format.name() + " -> ");
                encoder.encode(new MultimediaObject(source), outputFile, attrs);

                if (outputFile.exists() && outputFile.length() > 0) {
                    System.out.println("OK (" + outputFile.length() + " bytes)");
                } else {
                    System.out.println("ECHEC: Fichier vide ou non créé");
                }
            } catch (Exception e) {
                System.out.println("ECHEC EXCEPTION: " + e.getMessage());
            }
        }
        System.out.println("=== FIN DE TEST ===");
    }
}

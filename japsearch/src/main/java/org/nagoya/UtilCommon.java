package org.nagoya;

import io.vavr.control.Try;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class UtilCommon {

    public static final BiPredicate<Path, String> checkFileExt = (path, string) -> path.getFileName().toString().toLowerCase().endsWith(string);


    public static boolean renameFile(@NotNull Path oldName, @NotNull Path newName) {
        return Try.of(() -> Files.move(oldName, newName))
                .fold(e -> {
                    GUICommon.debugMessage(() -> ">> Error >> UtilCommon >> ---- Rename File ----");
                    return false;
                }, p -> true);
    }

    public static void delFile(Path file, Runnable onSuccess) {

        Try.run(() -> Files.delete(file)).onFailure(e -> {
            if (e instanceof DirectoryNotEmptyException) {

                GUICommon.showDialog("Directory is not Empty :", null, "Cancel", "Cont", () -> {

                    Try.withResources(() -> Files.walk(file)).of(s ->
                            s.sorted(Comparator.reverseOrder()).map(Path::toFile).map(File::delete).allMatch(i -> i == true)
                    ).onSuccess(v -> onSuccess.run());
                });
            } else {
                GUICommon.debugMessage(() -> ">> Error >> UtilCommon >> ---- Delete File ----");
            }

        }).onSuccess(v -> onSuccess.run());
    }

    public static boolean saveFile(URL url, File outputFile, String referer) {
        boolean isSucceed = true;

        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet(url.toString());
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.11 Safari/537.36");
        httpGet.addHeader("Referer", GUICommon.customReferrer(url, null));

        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity imageEntity = httpResponse.getEntity();

            if (imageEntity != null) {
                FileUtils.copyInputStreamToFile(imageEntity.getContent(), outputFile);
            }

        } catch (IOException e) {
            GUICommon.debugMessage(() -> ">> Error >> UtilCommon >> ---- Save File ----");
            isSucceed = false;
        }

        httpGet.releaseConnection();

        return isSucceed;
    }

    public static boolean saveFile(URL url, String outputPath) {

        int a = 5;
        Function<Integer, Integer> square = i -> i * i;
        int ans = square.apply(a);

        return saveFile(url, new File(outputPath), url.toString());
    }

    public static boolean saveFile(URL url, File outputFile) {

        return saveFile(url, outputFile, url.toString());
    }

    public static void saveStringToFile(@NotNull Path path, @NotNull String strJSon) {

        // GUICommon.debugMessage(() -> strJSon);

        File file = path.toFile();

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            try {
                fileOutputStream.write(strJSon.getBytes());
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                GUICommon.debugMessage(() -> ">> Error >> UtilCommon >> saveStringToFile >> IOException");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static String readStringFromFile(@NotNull Path path) {
        String text = "";

        try {
            File file = path.toFile();

            InputStream inputStream = new FileInputStream(file);
            StringBuilder stringBuilder = new StringBuilder();

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String receiveString = "";

            while ((receiveString = bufferedReader.readLine()) != null) {
                stringBuilder.append(receiveString);
            }

            inputStream.close();
            text = stringBuilder.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            GUICommon.debugMessage(() -> ">> Error >> UtilCommon >> readStringFromFile >> FileNotFound");
        } catch (IOException e) {
            e.printStackTrace();
            GUICommon.debugMessage(() -> ">> Error >> UtilCommon >> readStringFromFile >> IOException");
        }

        return text;
    }
}

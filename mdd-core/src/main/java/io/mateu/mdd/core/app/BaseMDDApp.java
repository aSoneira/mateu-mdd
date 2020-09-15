package io.mateu.mdd.core.app;

import io.mateu.mdd.core.MDD;
import io.mateu.mdd.core.data.FileLocator;
import io.mateu.mdd.core.interfaces.GeneralRepository;
import io.mateu.mdd.core.interfaces.UserPrincipal;
import io.mateu.mdd.util.common.Utils;
import io.mateu.mdd.util.Helper;
import io.mateu.mdd.util.mail.EmailHelper;
import io.mateu.mdd.util.persistence.JPATransaction;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
public abstract class BaseMDDApp extends AbstractApplication {


    public FileLocator upload(String fileName, byte[] bytes, boolean temporary)throws Throwable {

        String id = UUID.randomUUID().toString();
        String extension = ".tmp";
        if (fileName == null || "".equals(fileName.trim())) fileName = "" + id;
        if (fileName.lastIndexOf(".") < fileName.length() - 1) {
            extension = fileName.substring(fileName.lastIndexOf("."));
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
        }

        java.io.File temp = (System.getProperty("tmpdir") == null)? java.io.File.createTempFile(fileName, extension):new java.io.File(new java.io.File(System.getProperty("tmpdir")), fileName + extension);

        log.debug("java.io.tmpdir=" + System.getProperty("java.io.tmpdir"));
        log.debug("Temp file : " + temp.getAbsolutePath());

        if (true || !temp.exists()) {
            log.debug("writing temp file to " + temp.getAbsolutePath());
            Utils.write(temp, bytes);
        } else {
            log.debug("temp file already exists");
        }

        String baseUrl = System.getProperty("tmpurl");
        URL url = null;
        if (baseUrl == null) {
            url = temp.toURI().toURL();
        } else url = new URL(baseUrl + "/" + temp.getName());


        return new FileLocator(id, temp.getName(), url.toString(), temp.getAbsolutePath());
    }

    public void authenticate(String login, String password)throws Throwable {
        if (login == null || "".equals(login.trim()) || password == null || "".equals(password.trim())) throw new Exception("Username and password are required");

        UserPrincipal u = Helper.getImpl(GeneralRepository.class).authenticate(login, password);

        MDD.setCurrentUserLogin(u.getLogin());

    }


    public String recoverPassword(String login) throws Throwable {
        Helper.getImpl(GeneralRepository.class).recoverPassword(login);
        return "An email with instructions has been sent to you. Please check you inbox.";
    }

}

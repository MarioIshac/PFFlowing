package me.theeninja.pfflowing.gui.cardparser;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class GoogleDriveFetcher extends OnlineFileFetcher<File> {
    GoogleDriveFetcher() {
        super(File::getName, GoogleDriveFetcher::getCreatedTime);
    }

    private static Date getCreatedTime(File file) {
        return new Date(file.getCreatedTime().getValue());
    }

    private Drive.Files serviceFiles;

    @Override
    protected File newDummyFile() {
        File file = new File();
        file.setName("");

        DateTime dateTime = new DateTime(1);
        file.setCreatedTime(dateTime);

        return file;
    }

    @Override
    protected void setUpConnection() throws IOException {
        // Build a new authorized API client service.
        Drive service = GoogleDriveConnector.getDriveService();
        this.serviceFiles = service.files();
    }

    @Override
    protected List<File> getPossibleFiles() throws IOException {
        // Print the names and IDs for up to 10 files.
        FileList resultList = getServiceFiles().list()
                .setFields("nextPageToken, files(id, name, createdTime)")
                .setQ("mimeType = 'application/vnd.google-apps.document'")
                .execute();

        return resultList.getFiles();
    }

    @Override
    protected String getHTMLOfFile(File file) throws IOException {
        String selectedFileID = file.getId();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        getServiceFiles().export(selectedFileID, "text/html").executeMediaAndDownloadTo(outputStream);

        byte[] bytes = outputStream.toByteArray();

        return new String(bytes);
    }

    public Drive.Files getServiceFiles() {
        return serviceFiles;
    }
}

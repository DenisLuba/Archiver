package archiver;

import archiver.exception.PathIsNotFoundException;
import archiver.exception.WrongZipFileException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipFileManager {
    // Полный путь zip файла
    private final Path zipFile;

    public ZipFileManager(Path zipFile) {
        this.zipFile = zipFile;
    }

    public void createZip(Path source) throws Exception {
        // Проверяем, существует ли директория, где будет создаваться архив
        // При необходимости создаем ее
        Path zipDirectory = zipFile.getParent();
        if (Files.notExists(zipDirectory))
            Files.createDirectories(zipDirectory);

        // Создаем zip поток
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipFile))) {

            if (Files.isDirectory(source)) {
                // Если архивируем директорию, то нужно получить список файлов в ней
                FileManager fileManager = new FileManager(source);
                List<Path> fileNames = fileManager.getFileList();

                // Добавляем каждый файл в архив
                for (Path fileName : fileNames)
                    addNewZipEntry(zipOutputStream, source, fileName);

            } else if (Files.isRegularFile(source)) {

                // Если архивируем отдельный файл, то нужно получить его директорию и имя
                addNewZipEntry(zipOutputStream, source.getParent(), source.getFileName());
            } else {

                // Если переданный source не директория и не файл, бросаем исключение
                throw new PathIsNotFoundException();
            }
        }
    }

    public List<FileProperties> getFilesList() throws Exception {
        // Проверяем существует ли zip файл
        if (!Files.isRegularFile(zipFile)) {
            throw new WrongZipFileException();
        }

        List<FileProperties> files = new ArrayList<>();

        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry zipEntry = zipInputStream.getNextEntry();

            while (zipEntry != null) {
                // Поля "размер" и "сжатый размер" не известны, пока элемент не будет прочитан
                // Давайте вычитаем его в какой-то выходной поток
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                copyData(zipInputStream, baos);

                FileProperties file = new FileProperties(zipEntry.getName(), zipEntry.getSize(), zipEntry.getCompressedSize(), zipEntry.getMethod());
                files.add(file);
                zipEntry = zipInputStream.getNextEntry();
            }
        }

        return files;
    }

    public void extractAll(Path outputFolder) throws Exception {
        if (!Files.isRegularFile(zipFile)) throw new WrongZipFileException();
        if (Files.notExists(outputFolder)) Files.createDirectories(outputFolder);
        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                Path intoPath = Paths.get(zipEntry.getName());
                Path resultPath = outputFolder.resolve(intoPath);
                Path resultParent = resultPath.getParent();
                if (Files.notExists(resultPath)) Files.createDirectories(resultParent);
                try (OutputStream fileOutputStream = Files.newOutputStream(resultPath)) {
                    copyData(zipInputStream, fileOutputStream);
                }
            }
        }
    }

    public void removeFiles(List<Path> pathList) throws Exception {
        if (!Files.isRegularFile(zipFile)) throw new WrongZipFileException();
        Path tempPath = Files.createTempFile("zipFile", ".temp");
        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(zipFile));
             ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(tempPath))) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (pathList.contains(Paths.get(zipEntry.getName())))
                    ConsoleHelper.writeMessage("Файл " + zipEntry.getName() + " удален.");
                else {
                    zipOutputStream.putNextEntry(zipEntry);
                    copyData(zipInputStream, zipOutputStream);
                    zipInputStream.closeEntry();
                    zipOutputStream.closeEntry();
                }
            }
        }
        Files.move(tempPath, zipFile, StandardCopyOption.REPLACE_EXISTING);
        Files.deleteIfExists(tempPath);
    }

    public void removeFile(Path path) throws Exception {
        removeFiles(Collections.singletonList(path));
    }

    public void addFiles(List<Path> absolutePathList) throws Exception {
        if (!Files.isRegularFile(zipFile)) throw new WrongZipFileException();
        Path tempFile = Files.createTempFile("data", ".temp");
        try (ZipInputStream inputStream = new ZipInputStream(Files.newInputStream(zipFile));
             ZipOutputStream outputStream = new ZipOutputStream(Files.newOutputStream(tempFile))) {
            List<Path> listOldFiles = new ArrayList<>();
            ZipEntry entry;
            while ((entry = inputStream.getNextEntry()) != null) {
                outputStream.putNextEntry(entry);
                copyData(inputStream, outputStream);
                inputStream.closeEntry();
                outputStream.closeEntry();
                listOldFiles.add(Paths.get(entry.getName()));
            }
            for (Path absolutePath : absolutePathList) {
                if (!Files.isRegularFile(absolutePath)) throw new PathIsNotFoundException();
                Path relativePath = absolutePath.getFileName();
                if (listOldFiles.contains(relativePath))
                    ConsoleHelper.writeMessage("Файл " + absolutePath + " уже есть в архиве.");
                else {
                    addNewZipEntry(outputStream, absolutePath.getParent(), absolutePath.getFileName());
                    ConsoleHelper.writeMessage("Файл " + absolutePath + " добавлен в архив.");
                }
            }
            Files.move(tempFile, zipFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public void addFile(Path absolutePath) throws Exception {
        addFiles(Collections.singletonList(absolutePath));
    }

    private void addNewZipEntry(ZipOutputStream zipOutputStream, Path filePath, Path fileName) throws Exception {
        Path fullPath = filePath.resolve(fileName);
        try (InputStream inputStream = Files.newInputStream(fullPath)) {
            ZipEntry entry = new ZipEntry(fileName.toString());

            zipOutputStream.putNextEntry(entry);

            copyData(inputStream, zipOutputStream);

            zipOutputStream.closeEntry();
        }
    }

    private void copyData(InputStream in, OutputStream out) throws Exception {
        byte[] buffer = new byte[8 * 1024];
        int len;
        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
    }
}







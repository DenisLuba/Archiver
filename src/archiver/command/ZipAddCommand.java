package archiver.command;

import archiver.ConsoleHelper;
import archiver.ZipFileManager;
import archiver.exception.PathIsNotFoundException;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ZipAddCommand extends ZipCommand {
    @Override
    public void execute() throws Exception {
        try {
            ConsoleHelper.writeMessage("Введите полный путь к файлу, который вы хотите добавить в архив.");
            Path addPathFile = Paths.get(ConsoleHelper.readString());
            ZipFileManager zipFileManager = getZipFileManager();
            zipFileManager.addFile(addPathFile);
            ConsoleHelper.writeMessage("Добавление файлов в архив закончено.");
        } catch (PathIsNotFoundException e) {

        }
    }
}

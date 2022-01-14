package archiver.command;

import archiver.exception.WrongZipFileException;
import archiver.*;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ZipExtractCommand extends ZipCommand {
    @Override
    public void execute() throws Exception {
        try {
            ConsoleHelper.writeMessage("Распаковывание архива.");
            ZipFileManager zipFileManager = getZipFileManager();
            ConsoleHelper.writeMessage("Введите директорию для разархивированного файла:");
            Path outputFolder = Paths.get(ConsoleHelper.readString());
            zipFileManager.extractAll(outputFolder);
            ConsoleHelper.writeMessage("Архив разархивирован.");
        } catch (WrongZipFileException e) {
            ConsoleHelper.writeMessage("Вы неверно указали директорию.");
        }
    }
}

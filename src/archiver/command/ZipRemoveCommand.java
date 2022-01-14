package archiver.command;

import archiver.*;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ZipRemoveCommand extends ZipCommand {
    @Override
    public void execute() throws Exception {
        ConsoleHelper.writeMessage("Удаление архива.");

        ZipFileManager zipFileManager = getZipFileManager();

        ConsoleHelper.writeMessage("Введите имя файла для удаления:");
        Path removePath = Paths.get(ConsoleHelper.readString());
        zipFileManager.removeFile(removePath);

        ConsoleHelper.writeMessage("Файл удален.");
    }
}
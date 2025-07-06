package com.polylingoflow.ui;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * 管理应用程序的系统托盘图标和菜单。
 * 注意：此类使用 java.awt 类，在某些平台上可能会遇到集成挑战。
 */
public class TrayController {

    private static final Logger log = LoggerFactory.getLogger(TrayController.class);
    private final Stage stage;
    private TrayIcon trayIcon;

    public TrayController(Stage stage) {
        this.stage = stage;
    }

    /**
     * 创建系统托盘图标并设置其行为。
     */
    public void createTrayIcon() {
        if (!SystemTray.isSupported()) {
            log.warn("当前平台不支持系统托盘。");
            return;
        }

        // 确保在最后一个窗口关闭时，JavaFX平台不会退出。
        Platform.setImplicitExit(false);

        try {
            // 1. 从资源加载图标图像。
            // 请确保在 'src/main/resources/icons/app_icon.png' 路径下有一个16x16或32x32的PNG文件
            URL iconUrl = Objects.requireNonNull(getClass().getResource("/icons/app_icon.png"),
                    "图标资源未找到。请将 app_icon.png 放置在 resources/icons 文件夹中。");
            Image image = ImageIO.read(iconUrl);

            // 2. 为托盘图标创建弹出菜单。
            PopupMenu popup = createPopupMenu();

            // 3. 创建托盘图标（TrayIcon）。
            trayIcon = new TrayIcon(image, "PolylingoFlow", popup);
            trayIcon.setImageAutoSize(true);

            // 添加双击监听器以显示/隐藏主窗口。
            trayIcon.addActionListener(e -> Platform.runLater(this::toggleStageVisibility));

            // 4. 将图标添加到系统托盘。
            SystemTray.getSystemTray().add(trayIcon);
            log.info("系统托盘图标创建成功。");

            // 5. 设置当用户关闭主窗口时的行为。
            stage.setOnCloseRequest(event -> {
                event.consume(); // 阻止窗口实际关闭。
                hideStage();     // 而是将其隐藏。
            });

        } catch (IOException | NullPointerException | AWTException e) {
            log.error("创建系统托盘图标失败。", e);
        }
    }

    /**
     * 创建并配置托盘图标的弹出菜单。
     * @return 一个配置好的PopupMenu。
     */
    private PopupMenu createPopupMenu() {
        PopupMenu popup = new PopupMenu();

        // 用于显示/隐藏应用程序窗口的菜单项。
        MenuItem showItem = new MenuItem("显示 / 隐藏");
        showItem.addActionListener(e -> Platform.runLater(this::toggleStageVisibility));
        popup.add(showItem);

        popup.addSeparator();

        // 用于优雅退出应用程序的菜单项。
        MenuItem exitItem = new MenuItem("退出");
        exitItem.addActionListener(e -> {
            // 这是实现优雅关闭的关键部分。
            // 它会触发 MainUI.stop() 方法，该方法接着会调用 ApplicationManager.shutdown()。
            Platform.runLater(stage::close);
        });
        popup.add(exitItem);

        return popup;
    }

    /**
     * 切换主应用程序窗口（Stage）的可见性。
     */
    private void toggleStageVisibility() {
        if (stage.isShowing()) {
            stage.hide();
        } else {
            stage.show();
            stage.toFront(); // 将窗口置于最前。
        }
    }

    /**
     * 隐藏主应用程序窗口。
     */
    private void hideStage() {
        // 当应用最小化到托盘时，我们还可以显示一个通知气泡。
        if (trayIcon != null) {
            trayIcon.displayMessage(
                    "PolylingoFlow",
                    "应用程序正在后台运行。",
                    TrayIcon.MessageType.INFO
            );
        }
        stage.hide();
    }
}
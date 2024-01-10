import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class main {
    private JPanel panel1;
    private JButton 开始备份Button;
    private JTabbedPane tabbedPane1;
    private JButton 清空Button;
    private JButton 添加Button;
    private JButton 删除Button;
    private JFormattedTextField formattedTextField1;
    private JTextField textField1;
    private JButton 选择备份位置;
    private JScrollPane scroll;
    private JTable table1;
    private JTextField textField2;
    private JButton 添加恢复文件;
    private JButton 恢复Button;
    private JPanel 恢复;
    private JPanel 备份;
    private JPanel 任务;
    private JCheckBox 是否需要密码CheckBox;
    private JPasswordField passwordField1;
    private JTable table2;
    private JCheckBox 是否上传到云端;
    private JButton 刷新云端文件列表Button;
    private JButton 上传文件Button;
    private JButton 删除选定备份文件Button;
    private JButton 还原选定备份文件Button;
    private JScrollPane scrollpane;
    private JTextField textField3;
    private JButton button1;
    private JRadioButton 定时备份RadioButton;
    private JComboBox comboBox1;
    private JTable table3;
    private JButton 删除Button1;
    private JButton 清空Button1;
    private JButton 比较文件一致性Button;
    private JTextField textField4;
    private JButton button3;
    private JTextField textField5;
    private JButton button4;

    DefaultTableModel model2;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final List<ScheduledFuture<?>> scheduledTasks = new ArrayList<>();

    DefaultTableModel model3;

    private void refresh() {
        String jsonResponse = FileClient.getAllFiles("http://127.0.0.1:5000/filelist");
        JSONObject jsonResult = new JSONObject(jsonResponse);
        JSONArray filesArray = jsonResult.getJSONArray("files");
        model2.setRowCount(0);
        for (int i = 0; i < filesArray.length(); i++) {
            JSONObject fileObject = filesArray.getJSONObject(i);
            String fileName = fileObject.getString("filename");
            String timestamp = fileObject.getString("created_time");

            // Convert timestamp to the desired format
            String formattedTime = convertTimestamp(timestamp);

            // Add a new row to the table model
            String[] rowData = {fileName, formattedTime};
            model2.addRow(rowData);
        }
    }

    private boolean areFilesIdentical(String filePath1, String filePath2) throws IOException {
        Path path1 = Paths.get(filePath1);
        Path path2 = Paths.get(filePath2);

        try (FileChannel channel1 = FileChannel.open(path1);
             FileChannel channel2 = FileChannel.open(path2)) {

            ByteBuffer buffer1 = ByteBuffer.allocate(8192);
            ByteBuffer buffer2 = ByteBuffer.allocate(8192);

            while (channel1.read(buffer1) > 0 || channel2.read(buffer2) > 0) {
                buffer1.flip();
                buffer2.flip();

                while (buffer1.hasRemaining() && buffer2.hasRemaining()) {
                    if (buffer1.get() != buffer2.get()) {
                        return false;
                    }
                }

                buffer1.compact();
                buffer2.compact();
            }

            return buffer1.position() == buffer2.position();
        }
    }

    private void extracted(String sourcepath) {

        String[] strings = sourcepath.split("\\.");
        File file = new File(sourcepath);
        if (Objects.equals(strings[strings.length - 1], "encry")) {
            String psd = JOptionPane.showInputDialog(恢复, "请输入密码:");
            try {
                int deresult = DeXOR.decryptFile(sourcepath, strings[strings.length - 2] + ".decry", psd);
                if (deresult == 0) {
                    JOptionPane.showMessageDialog(恢复, "解密成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    int unzipresult = unzip.decompressFile(strings[strings.length - 2] + ".decry", file.getParent());
                    if (unzipresult == 0) {
                        JOptionPane.showMessageDialog(恢复, "解压成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                        int result = Unpack.unpack(strings[strings.length - 2] + ".unzip", file.getParent() + "\\unpacked");
                        if (result == 0) {
                            JOptionPane.showMessageDialog(恢复, "解包成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(恢复, ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                throw new RuntimeException(ex);
            }
        } else if (Objects.equals(strings[strings.length - 1], "zip")) {
            try {
                int unzipresult = unzip.decompressFile(sourcepath, file.getParent());
                if (unzipresult == 0) {
                    JOptionPane.showMessageDialog(恢复, "解压成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    int result = Unpack.unpack(strings[strings.length - 2] + ".unzip", file.getParent() + "\\unpacked");
                    if (result == 0) {
                        JOptionPane.showMessageDialog(恢复, "解包成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (Exception ee) {
                ee.printStackTrace();
                JOptionPane.showMessageDialog(恢复, ee.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(恢复, "请选择正确的待恢复文件！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    public main() {
        $$$setupUI$$$();
        DefaultComboBoxModel<String> combomodel = (DefaultComboBoxModel<String>) comboBox1.getModel();
        combomodel.addElement("每天");
        combomodel.addElement("每周");
        combomodel.addElement("每月");
        DefaultTableModel model = new DefaultTableModel() {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.addColumn("文件名");
        model.addColumn("类型");
        model.addColumn("大小/字节");
        model.addColumn("路径");
        model.addColumn("创建时间");
        model.addColumn("修改时间");
        model.addColumn("访问时间");


        table1.setModel(model);
        TableRowSorter<DefaultTableModel> tableRowSorter = new TableRowSorter<>(model);
        table1.setRowSorter(tableRowSorter);

        model2 = new DefaultTableModel() {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model2.addColumn("文件名");
        model2.addColumn("上传时间");
        table2.setModel(model2);

        model3 = new DefaultTableModel() {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model3.addColumn("备份文件名");
        model3.addColumn("备份文件列表");
        model3.addColumn("备份频率");
        model3.addColumn("备份位置");
        table3.setModel(model3);

        选择备份位置.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                JFileChooser BackupLocationChooser = new JFileChooser();
                BackupLocationChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                BackupLocationChooser.showDialog(new JLabel(), "选择");
                File file = BackupLocationChooser.getSelectedFile();
                textField1.setText(file.toString());
            }
        });
        添加Button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                fileChooser.setMultiSelectionEnabled(true);
                FileNameExtensionFilter fileNameExtensionFilter = new FileNameExtensionFilter("图片文件(*.jpg,*.png,*.gif)", "png", "jpg", "gif");
                fileChooser.setFileFilter(fileNameExtensionFilter);
                fileChooser.addChoosableFileFilter(new SizeFilter());
                int result = fileChooser.showOpenDialog(new JFrame());
                if (result == JFileChooser.APPROVE_OPTION) {
                    File[] selectedFiles = fileChooser.getSelectedFiles();
                    if (selectedFiles != null) {
                        for (File file : selectedFiles)
                            if (file != null) {
                                String fileName = file.getName();
                                String filePath = file.getAbsolutePath();
                                String fileType = getFileType(file);
                                String[] fileAttributes = getFileAttributes(file);

                                // 添加一行到表格，包含文件名、路径、类型、时间和尺寸信息
                                String[] rowData = {fileName, fileType, fileAttributes[3], filePath, fileAttributes[0], fileAttributes[1], fileAttributes[2]};
                                model.addRow(rowData);
                                setToolTipForSizeColumn();
                            }
                    }
                }
            }

            private void setToolTipForSizeColumn() {
                TableColumn sizeColumn = table1.getColumnModel().getColumn(2); // 6 是 "Size" 列的索引
                sizeColumn.setCellRenderer(new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        if (value != null) {
                            long size = Long.parseLong(value.toString());
                            String sizeText = formatSize(size);
                            setToolTipText(sizeText); // 设置 ToolTip
                        }
                        return component;
                    }
                });
            }

            private static class SizeFilter extends FileFilter {
                @Override
                public boolean accept(File file) {
                    // 这里可以根据文件大小进行筛选，例如只允许大小小于等于1MB的文件
                    return file.isDirectory() || (file.isFile() && file.length() >= 1024 * 1024 * 100); // 1MB
                }

                @Override
                public String getDescription() {
                    return "大于等于100MB的文件";
                }
            }

            private String formatSize(long size) {
                if (size < 1024) {
                    return size + " bytes";
                } else if (size < 1024 * 1024) {
                    return String.format("%.2f KB", size / 1024.0);
                } else {
                    return String.format("%.2f MB", size / (1024.0 * 1024));
                }
            }

            private String getFileType(File file) {
                return file.isDirectory() ? "文件夹" : "文件";
            }

            private String[] getFileAttributes(File file) {
                String[] attributes = new String[4];

                try {
                    Path filePath = file.toPath();
                    BasicFileAttributes fileAttributes = Files.readAttributes(filePath, BasicFileAttributes.class);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    attributes[0] = dateFormat.format(fileAttributes.creationTime().toMillis());
                    attributes[1] = dateFormat.format(fileAttributes.lastModifiedTime().toMillis());
                    attributes[2] = dateFormat.format(fileAttributes.lastAccessTime().toMillis());
                    attributes[3] = String.valueOf(file.length());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return attributes;
            }
        });


        删除Button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                int selectedRow = table1.getSelectedRow();
                if (selectedRow != -1) {
                    // 移除选中的行
                    model.removeRow(selectedRow);
                } else {
                    JOptionPane.showMessageDialog(panel1, "请选择要删除的行");
                }
            }
        });
        清空Button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                model.setRowCount(0);
            }
        });
        开始备份Button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                List<String> filesToPack = new ArrayList<>();
                for (int row = 0; row < model.getRowCount(); row++) {
                    // 假设文件路径在第一列
                    String filePath = (String) model.getValueAt(row, 3);
                    filesToPack.add(filePath);
                }
                String text = textField1.getText();
                if (!text.isEmpty()) {
                    // 获取最后一个字符
                    char lastChar = text.charAt(text.length() - 1);
                    System.out.println("最后一个字符是：" + text);
                    if (lastChar != '\\') {
                        text += '\\';
                    }
                } else {
                    JOptionPane.showMessageDialog(备份, "备份位置为空", "错误", JOptionPane.ERROR_MESSAGE);
                }
                if (formattedTextField1.getText().contains(".")) {
                    JOptionPane.showMessageDialog(备份, "备份文件名中不允许出现dot(.)");
                } else {
                    String destinationPath = text + formattedTextField1.getText();
                    System.out.println(destinationPath);
                    try {
                        int result = Pack.pack(filesToPack, destinationPath);
                        if (result == 0) {
                            JOptionPane.showMessageDialog(备份, "打包成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                            int zipresult = zip.compressFile(destinationPath, destinationPath + ".zip");
                            if (zipresult == 0) {
                                JOptionPane.showMessageDialog(备份, "压缩成功，如您选择了加密，请等待加密信息提示框弹出后再进行下一步操作", "成功", JOptionPane.INFORMATION_MESSAGE);
                                if (是否需要密码CheckBox.isSelected()) {
                                    int encryresult = XOR.encryptFile(destinationPath + ".zip", destinationPath + ".encry", passwordField1.getText());
                                    if (encryresult == 0) {
                                        JOptionPane.showMessageDialog(备份, "加密成功，你的密码是:" + passwordField1.getText(), "成功", JOptionPane.INFORMATION_MESSAGE);
                                        if (是否上传到云端.isSelected()) {
                                            File file = new File(destinationPath + ".encry");
                                            FileClient.uploadFile("http://127.0.0.1:5000/file/" + file.getName(), file.getAbsolutePath());
                                        }
                                        if (定时备份RadioButton.isSelected()) {
                                            String selectedTimeInterval = (String) comboBox1.getSelectedItem();  // Replace with the actual value from your comboBox
                                            System.out.println(selectedTimeInterval);
                                            int intervalInSeconds = calculateInterval(selectedTimeInterval);
                                            System.out.println(intervalInSeconds);
                                            ScheduledFuture<?> scheduledTask = scheduler.scheduleAtFixedRate(() -> {
                                                System.out.println("Scheduled task executed");
                                                Pack.pack(filesToPack, destinationPath);
                                                zip.compressFile(destinationPath, destinationPath + ".zip");
                                                try {
                                                    XOR.encryptFile(destinationPath + ".zip", destinationPath + ".encry", passwordField1.getText());
                                                } catch (Exception ex) {
                                                    throw new RuntimeException(ex);
                                                }
                                            }, intervalInSeconds, intervalInSeconds, TimeUnit.SECONDS);
                                            SwingUtilities.invokeLater(() -> updateTable3(selectedTimeInterval, formattedTextField1.getText(), filesToPack, destinationPath));
                                            scheduledTasks.add(scheduledTask);

                                        }
                                    } else {
                                        JOptionPane.showMessageDialog(备份, "加密失败", "错误", JOptionPane.ERROR_MESSAGE);
                                    }
                                } else {
                                    if (是否上传到云端.isSelected()) {
                                        File file = new File(destinationPath + ".zip");
                                        FileClient.uploadFile("http://127.0.0.1:5000/file/" + file.getName(), file.getAbsolutePath());
                                    }
                                    if (定时备份RadioButton.isSelected()) {
                                        String selectedTimeInterval = (String) comboBox1.getSelectedItem();  // Replace with the actual value from your comboBox
                                        System.out.println(selectedTimeInterval);
                                        int intervalInSeconds = calculateInterval(selectedTimeInterval);
                                        System.out.println(intervalInSeconds);
                                        ScheduledFuture<?> scheduledTask = scheduler.scheduleAtFixedRate(() -> {
                                            System.out.println("Scheduled task executed");
                                            Pack.pack(filesToPack, destinationPath);
                                            zip.compressFile(destinationPath, destinationPath + ".zip");
                                        }, intervalInSeconds, intervalInSeconds, TimeUnit.SECONDS);
                                        SwingUtilities.invokeLater(() -> updateTable3(selectedTimeInterval, formattedTextField1.getText(), filesToPack, destinationPath));
                                        scheduledTasks.add(scheduledTask);

                                    }
                                }
                            } else {
                                JOptionPane.showMessageDialog(备份, "压缩失败", "错误", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    } catch (RuntimeException exception) {
                        JOptionPane.showMessageDialog(备份, exception.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
        添加恢复文件.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                JFileChooser recoverfile = new JFileChooser();
                recoverfile.setFileSelectionMode(JFileChooser.FILES_ONLY);
                recoverfile.showDialog(new JLabel(), "选择");
                File file = recoverfile.getSelectedFile();
                textField2.setText(file.toString());
            }
        });
        恢复Button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                String path = textField2.getText();
                extracted(path);

            }
        });


        是否需要密码CheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    passwordField1.setEditable(true);
                } else {
                    passwordField1.setEditable(false);
                    passwordField1.setText(null);

                }
            }
        });
        刷新云端文件列表Button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                refresh();
            }
        });
        删除选定备份文件Button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                int selectedRow = table2.getSelectedRow();
                if (selectedRow != -1) {
                    String filename = (String) model2.getValueAt(selectedRow, 0);
                    // 移除选中的行
                    System.out.println(filename);
                    FileClient.deletefile("http://127.0.0.1:5000/file/" + filename);
                    refresh();
                } else {
                    JOptionPane.showMessageDialog(panel1, "请选择要删除的行");
                }
            }
        });
        上传文件Button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setMultiSelectionEnabled(true);
                int result = fileChooser.showOpenDialog(new JFrame());
                if (result == JFileChooser.APPROVE_OPTION) {
                    File[] selectedFiles = fileChooser.getSelectedFiles();
                    if (selectedFiles != null) {
                        for (File file : selectedFiles)
                            if (file != null) {
                                String fileName = file.getName();
                                String filePath = file.getAbsolutePath();
                                FileClient.uploadFile("http://127.0.0.1:5000/file/" + fileName, filePath);
                            }
                    }
                }
                refresh();

            }
        });
        还原选定备份文件Button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                int selectedRow = table2.getSelectedRow();
                if (selectedRow != -1) {
                    String filename = (String) model2.getValueAt(selectedRow, 0);
                    String path = textField3.getText();
                    if (!path.isEmpty()) {
                        // 获取最后一个字符
                        char lastChar = path.charAt(path.length() - 1);
                        if (lastChar != '\\') {
                            path += '\\';
                        }
                        FileClient.downloadFile("http://127.0.0.1:5000/file/" + filename, path + filename);
                        extracted(path + filename);
                    } else {
                        JOptionPane.showMessageDialog(备份, "还原位置为空", "错误", JOptionPane.ERROR_MESSAGE);
                    }

                } else {
                    JOptionPane.showMessageDialog(panel1, "请选择还原的文件");
                }
            }
        });
        button1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                JFileChooser BackupLocationChooser = new JFileChooser();
                BackupLocationChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                BackupLocationChooser.showDialog(new JLabel(), "选择");
                File file = BackupLocationChooser.getSelectedFile();
                textField3.setText(file.toString());
            }
        });
        删除Button1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                int[] selectedRows = table3.getSelectedRows();
                for (int selectedRow : selectedRows) {
                    scheduledTasks.get(selectedRow).cancel(true);
                    model3.removeRow(selectedRow);
                }
            }
        });
        清空Button1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                for (ScheduledFuture<?> scheduledTask : scheduledTasks) {
                    scheduledTask.cancel(true);
                }
                model3.setRowCount(0);
            }
        });
        button3.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                jFileChooser.showDialog(new JLabel(), "选择");
                File file = jFileChooser.getSelectedFile();
                textField4.setText(file.toString());
            }
        });
        button4.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                jFileChooser.showDialog(new JLabel(), "选择");
                File file = jFileChooser.getSelectedFile();
                textField5.setText(file.toString());
            }
        });
        比较文件一致性Button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                String filePath1 = textField4.getText();
                String filePath2 = textField5.getText();

                if (!filePath1.isEmpty() && !filePath2.isEmpty()) {
                    try {
                        boolean areFilesSame = areFilesIdentical(filePath1, filePath2);
                        if (areFilesSame) {
                            System.out.println("The files are the same.");
                            JOptionPane.showMessageDialog(恢复, "两文件一致", "结果", JOptionPane.INFORMATION_MESSAGE);

                        } else {
                            System.out.println("The files are different.");
                            JOptionPane.showMessageDialog(恢复, "两文件不一致", "结果", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (IOException eee) {
                        eee.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(恢复, "请选择要比较的文件", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        passwordField1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                passwordField1.setEchoChar((char) 0);
            }
        });
        passwordField1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                passwordField1.setEchoChar('*');
            }
        });
    }


    private void updateTable3(String selectedTimeInterval, String name, List<String> list, String path) {
        SwingUtilities.invokeLater(() -> {
            Object[] rowData = {name, list, selectedTimeInterval, path};  // Replace with actual details
            model3.addRow(rowData);
        });
    }

    private String convertTimestamp(String timestamp) {
        // Define a custom formatter with support for milliseconds
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");

        // Parse the timestamp as a LocalDateTime
        LocalDateTime dateTime = LocalDateTime.parse(timestamp, formatter);

        // Format the LocalDateTime as a custom string
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss");
        return dateTime.format(outputFormatter);
    }

    private int calculateInterval(String selectedTimeInterval) {
        switch (selectedTimeInterval) {
            case "每天":
                return 24 * 60 * 60;
            case "每周":
                return 7 * 24 * 60 * 60;
            case "每月":
                return 30 * 24 * 60 * 60;
            default:
                return 0;
        }


    }

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        JFrame frame = new JFrame("数据备份");
        frame.setContentPane(new main().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setSize(1000, 800);
    }


    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.setForeground(new Color(-13482824));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel2.setBorder(BorderFactory.createTitledBorder(null, "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        tabbedPane1 = new JTabbedPane();
        tabbedPane1.setInheritsPopupMenu(false);
        tabbedPane1.setName("filetable");
        panel2.add(tabbedPane1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        备份 = new JPanel();
        备份.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 13, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("备份", 备份);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
        备份.add(panel3, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 12, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        清空Button = new JButton();
        清空Button.setText("清空");
        panel3.add(清空Button, new com.intellij.uiDesigner.core.GridConstraints(1, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        添加Button = new JButton();
        添加Button.setText("添加");
        panel3.add(添加Button, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        删除Button = new JButton();
        删除Button.setText("删除");
        panel3.add(删除Button, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        panel3.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, new Dimension(553, 11), null, 0, false));
        scroll = new JScrollPane();
        scroll.setEnabled(true);
        scroll.setVisible(true);
        panel3.add(scroll, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scroll.setBorder(BorderFactory.createTitledBorder(null, "待备份列表", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        table1 = new JTable();
        table1.setDragEnabled(false);
        table1.setEnabled(true);
        scroll.setViewportView(table1);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(4, 7, new Insets(0, 0, 0, 0), -1, -1));
        备份.add(panel4, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 12, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel4.setBorder(BorderFactory.createTitledBorder(null, "选项", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        开始备份Button = new JButton();
        开始备份Button.setText("开始备份");
        panel4.add(开始备份Button, new com.intellij.uiDesigner.core.GridConstraints(3, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("备份文件名");
        panel4.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("备份位置");
        panel4.add(label2, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        formattedTextField1 = new JFormattedTextField();
        formattedTextField1.setText("");
        panel4.add(formattedTextField1, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        textField1 = new JTextField();
        textField1.setEnabled(false);
        textField1.setVisible(true);
        panel4.add(textField1, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
        panel4.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(1, 6, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer3 = new com.intellij.uiDesigner.core.Spacer();
        panel4.add(spacer3, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        passwordField1 = new JPasswordField();
        passwordField1.setEditable(false);
        passwordField1.setEnabled(true);
        passwordField1.setText("");
        panel4.add(passwordField1, new com.intellij.uiDesigner.core.GridConstraints(2, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("密码");
        panel4.add(label3, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBox1 = new JComboBox();
        panel4.add(comboBox1, new com.intellij.uiDesigner.core.GridConstraints(3, 5, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        是否上传到云端 = new JCheckBox();
        是否上传到云端.setSelected(false);
        是否上传到云端.setText("备份文件上传到云端");
        panel4.add(是否上传到云端, new com.intellij.uiDesigner.core.GridConstraints(3, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        是否需要密码CheckBox = new JCheckBox();
        是否需要密码CheckBox.setSelected(false);
        是否需要密码CheckBox.setText("是否需要密码");
        panel4.add(是否需要密码CheckBox, new com.intellij.uiDesigner.core.GridConstraints(2, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        选择备份位置 = new JButton();
        选择备份位置.setHideActionText(false);
        选择备份位置.setText("...");
        选择备份位置.setVerticalAlignment(1);
        panel4.add(选择备份位置, new com.intellij.uiDesigner.core.GridConstraints(1, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        定时备份RadioButton = new JRadioButton();
        定时备份RadioButton.setText("定时备份");
        panel4.add(定时备份RadioButton, new com.intellij.uiDesigner.core.GridConstraints(3, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        恢复 = new JPanel();
        恢复.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(4, 21, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("恢复", 恢复);
        恢复.setBorder(BorderFactory.createTitledBorder(null, "本地恢复", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 7, new Insets(0, 0, 0, 0), -1, -1));
        恢复.add(panel5, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 21, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel5.setBorder(BorderFactory.createTitledBorder(null, "云端文件列表", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        scrollpane = new JScrollPane();
        panel5.add(scrollpane, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 7, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        table2 = new JTable();
        scrollpane.setViewportView(table2);
        textField4 = new JTextField();
        textField4.setEditable(false);
        textField4.setText("");
        恢复.add(textField4, new com.intellij.uiDesigner.core.GridConstraints(1, 7, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        button3 = new JButton();
        button3.setText("...");
        恢复.add(button3, new com.intellij.uiDesigner.core.GridConstraints(1, 8, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textField5 = new JTextField();
        textField5.setEditable(false);
        textField5.setText("");
        恢复.add(textField5, new com.intellij.uiDesigner.core.GridConstraints(1, 9, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        button4 = new JButton();
        button4.setText("...");
        恢复.add(button4, new com.intellij.uiDesigner.core.GridConstraints(1, 10, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer4 = new com.intellij.uiDesigner.core.Spacer();
        恢复.add(spacer4, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 5, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        textField2 = new JTextField();
        textField2.setEnabled(false);
        恢复.add(textField2, new com.intellij.uiDesigner.core.GridConstraints(0, 7, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(268, 30), null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer5 = new com.intellij.uiDesigner.core.Spacer();
        恢复.add(spacer5, new com.intellij.uiDesigner.core.GridConstraints(0, 15, 1, 6, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        添加恢复文件 = new JButton();
        添加恢复文件.setHideActionText(false);
        添加恢复文件.setText("...");
        添加恢复文件.setVerticalAlignment(1);
        恢复.add(添加恢复文件, new com.intellij.uiDesigner.core.GridConstraints(0, 9, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(186, 30), null, 0, false));
        恢复Button = new JButton();
        恢复Button.setText("恢复");
        恢复.add(恢复Button, new com.intellij.uiDesigner.core.GridConstraints(0, 10, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(177, 30), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("请选择要恢复的文件");
        恢复.add(label4, new com.intellij.uiDesigner.core.GridConstraints(0, 6, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, 17), null, 0, false));
        比较文件一致性Button = new JButton();
        比较文件一致性Button.setText("比较文件一致性");
        恢复.add(比较文件一致性Button, new com.intellij.uiDesigner.core.GridConstraints(1, 6, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer6 = new com.intellij.uiDesigner.core.Spacer();
        恢复.add(spacer6, new com.intellij.uiDesigner.core.GridConstraints(0, 14, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer7 = new com.intellij.uiDesigner.core.Spacer();
        恢复.add(spacer7, new com.intellij.uiDesigner.core.GridConstraints(0, 5, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer8 = new com.intellij.uiDesigner.core.Spacer();
        恢复.add(spacer8, new com.intellij.uiDesigner.core.GridConstraints(0, 13, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer9 = new com.intellij.uiDesigner.core.Spacer();
        恢复.add(spacer9, new com.intellij.uiDesigner.core.GridConstraints(0, 12, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer10 = new com.intellij.uiDesigner.core.Spacer();
        恢复.add(spacer10, new com.intellij.uiDesigner.core.GridConstraints(0, 11, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 8, new Insets(0, 0, 0, 0), -1, -1));
        恢复.add(panel6, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 21, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel6.setBorder(BorderFactory.createTitledBorder(null, "云端选项", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        上传文件Button = new JButton();
        上传文件Button.setText("上传文件");
        panel6.add(上传文件Button, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        删除选定备份文件Button = new JButton();
        删除选定备份文件Button.setText("删除选定备份文件");
        panel6.add(删除选定备份文件Button, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        还原选定备份文件Button = new JButton();
        还原选定备份文件Button.setText("还原选定备份文件");
        panel6.add(还原选定备份文件Button, new com.intellij.uiDesigner.core.GridConstraints(0, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("还原到");
        panel6.add(label5, new com.intellij.uiDesigner.core.GridConstraints(0, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textField3 = new JTextField();
        textField3.setEditable(false);
        panel6.add(textField3, new com.intellij.uiDesigner.core.GridConstraints(0, 5, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        button1 = new JButton();
        button1.setText("...");
        panel6.add(button1, new com.intellij.uiDesigner.core.GridConstraints(0, 6, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        刷新云端文件列表Button = new JButton();
        刷新云端文件列表Button.setText("刷新云端文件列表");
        panel6.add(刷新云端文件列表Button, new com.intellij.uiDesigner.core.GridConstraints(0, 7, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer11 = new com.intellij.uiDesigner.core.Spacer();
        panel6.add(spacer11, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        任务 = new JPanel();
        任务.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("任务", 任务);
        final JScrollPane scrollPane1 = new JScrollPane();
        任务.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPane1.setBorder(BorderFactory.createTitledBorder(null, "定时任务表", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        table3 = new JTable();
        scrollPane1.setViewportView(table3);
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        任务.add(panel7, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel7.setBorder(BorderFactory.createTitledBorder(null, "选项", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final com.intellij.uiDesigner.core.Spacer spacer12 = new com.intellij.uiDesigner.core.Spacer();
        panel7.add(spacer12, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        清空Button1 = new JButton();
        清空Button1.setText("清空");
        panel7.add(清空Button1, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        删除Button1 = new JButton();
        删除Button1.setText("删除");
        panel7.add(删除Button1, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }


}

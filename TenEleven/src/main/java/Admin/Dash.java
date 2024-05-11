package Admin;

import static Admin.Facerecog.recognizeFace;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import org.opencv.imgcodecs.Imgcodecs;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Dash extends javax.swing.JFrame implements WindowFocusListener {
    
    private VideoCapture capture;
    private DaemonThread myThread;
    private CascadeClassifier faceCascade;
    private boolean windowFocused = true; // Flag to track window focus state
    

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public Dash() {
        initComponents();
        startCamera();
        initializeFaceCascade();
        startLogListUpdater();
          addWindowFocusListener(this);
        
        // Add window listener to handle window closing event
        addWindowListener(new WindowAdapter() {
            
        });
        
                

    }
    
    ////// winsow
    
     public void windowGainedFocus(WindowEvent e) {
        windowFocused = true;

        
    }

    public void windowLostFocus(WindowEvent e) {
        windowFocused = false;
         

       
    }
    
    
    //////

    private void initializeFaceCascade() {
        faceCascade = new CascadeClassifier("D:\\TenEleven\\TenEleven\\employees\\haarcascade_frontalface_default.xml");
        if (faceCascade.empty()) {
            System.out.println("Failed to load Haar cascade");
        }
    }

    private void startCamera() {
        
        capture = new VideoCapture(0); // 0 represents the default camera
        myThread = new DaemonThread();
        Thread t = new Thread(myThread);
        t.setDaemon(true);
        myThread.runnable = true;
        t.start();
    }
 private void stopCamera() {
    if (capture != null && capture.isOpened()) {
        myThread.runnable = false; // Set the flag to stop the thread
        capture.release(); // Release the camera
    }
}




    class DaemonThread implements Runnable {
        protected volatile boolean runnable = false;

        @Override
        public void run() {
            synchronized (this) {
                while (runnable) {
                    if (capture.isOpened()) {
                        Mat frame = new Mat();
                        capture.read(frame);
                        if (!frame.empty()) {
                            detectAndDrawFaces(frame);
                        }
                    }
                }
            }
        }
    }

   private Set<String> detectedFaces = new HashSet<>(); // Store the IDs of detected faces
    private long lastCaptureTime = 0; // Track the time of the last captured picture
    private ExecutorService executor = Executors.newFixedThreadPool(5); // Create a thread pool with 5 threads

    private void detectAndDrawFaces(Mat frame) {
        MatOfRect faces = new MatOfRect();
        faceCascade.detectMultiScale(frame, faces);

        Rect[] facesArray = faces.toArray();
        for (Rect rect : facesArray) {
            String faceId = getFaceId(rect); // Get a unique identifier for the detected face
            
            // Check if this is a new face
            if (!detectedFaces.contains(faceId) && windowFocused) {
                // Draw green rectangle
                Imgproc.rectangle(frame, rect.tl(), rect.br(), new Scalar(0, 255, 0), 3);
                
                // Save the frame when a new face is recognized
                System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
                String imagePath = "C:\\PROJECTS\\cam.jpg";
                Imgcodecs.imwrite(imagePath, frame);
                
                // Submit facial recognition task to the thread pool
                executor.submit(() -> recognizeAndLogFace(imagePath, faceId));
                
                // Update the set of detected faces
                detectedFaces.add(faceId);
            }
        }

        // Display the frame with detected faces
        BufferedImage img = matToBufferedImage(frame);
        Graphics g = webcampanel.getGraphics();
        g.drawImage(img, 0, 0, webcampanel.getWidth(), webcampanel.getHeight(), null);
    }

    private void recognizeAndLogFace(String imagePath, String faceId) {
        try {
            String[] result = recognizeFace(imagePath);
            
            // Get direction from the combo box
            String direction = camera.getSelectedItem().toString();
            direction= direction.toLowerCase();
            
            // Get current time
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String time = dtf.format(now);
            
            // Call writeLog method using the result
            DBWriter logWriter = new DBWriter(); // Create an instance
            if(direction != null){
            logWriter.addLog(result[0], direction, time, result[1]);}
            //
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    // Generate a unique identifier for the detected face based on its position
    private String getFaceId(Rect rect) {
        return rect.x + "_" + rect.y + "_" + rect.width + "_" + rect.height;
    }

    private BufferedImage matToBufferedImage(Mat frame) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (frame.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = frame.channels() * frame.cols() * frame.rows();
        byte[] b = new byte[bufferSize];
        frame.get(0, 0, b);
        BufferedImage img = new BufferedImage(frame.cols(), frame.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);
        return img;
    }
    
    
    
    //////// log list
    
       private void startLogListUpdater() {
        Thread logListUpdater = new Thread(() -> {
            while (true) {
                try {
                                DBWriter logWriter = new DBWriter(); // Create an instance

                    // Fetch logs
                    String[] logs = logWriter.getLast10Logs();

                    // Update loglist
                    updateLogList(logs);

                    // Sleep for 1 second
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (SQLException ex) {
                    Logger.getLogger(Dash.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        logListUpdater.setDaemon(true);
        logListUpdater.start();
    }

   

    private void updateLogList(String[] logs) {
        SwingUtilities.invokeLater(() -> {
            DefaultListModel<String> model = new DefaultListModel<>();
            for (String log : logs) {
                model.addElement(log);
            }
            loglist.setModel(model);
        });
    }

   
    
    
    /////////
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenu1 = new javax.swing.JMenu();
        webcampanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        title = new javax.swing.JLabel();
        settings = new javax.swing.JButton();
        stats = new javax.swing.JButton();
        addemp = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        loglist = new javax.swing.JList<>();
        camera = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();

        jMenu1.setText("jMenu1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        webcampanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout webcampanelLayout = new javax.swing.GroupLayout(webcampanel);
        webcampanel.setLayout(webcampanelLayout);
        webcampanelLayout.setHorizontalGroup(
            webcampanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 503, Short.MAX_VALUE)
        );
        webcampanelLayout.setVerticalGroup(
            webcampanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 303, Short.MAX_VALUE)
        );

        jPanel2.setBackground(new java.awt.Color(132, 160, 255));

        title.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        title.setText("Admin Dashboard");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(title, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(16, Short.MAX_VALUE)
                .addComponent(title)
                .addGap(15, 15, 15))
        );

        settings.setText("Settings");
        settings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsActionPerformed(evt);
            }
        });

        stats.setText("Statistics");
        stats.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statsActionPerformed(evt);
            }
        });

        addemp.setText("Add employee");
        addemp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addempActionPerformed(evt);
            }
        });

        loglist.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(loglist);

        camera.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        camera.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "In", "Out" }));
        camera.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cameraActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setText("Camera:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 658, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(camera, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(webcampanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(settings, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(stats, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(addemp, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(15, 15, 15))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(settings, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stats, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addemp, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(95, 95, 95)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(camera, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(35, 35, 35)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1)
                    .addComponent(webcampanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(18, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void settingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsActionPerformed
        // TODO add your handling code here:
          JFrame settings = new JFrame("Settings");
    settings.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Dispose the frame on close
    settings.getContentPane().add(new Settings());
    settings.pack();
    settings.setVisible(true);
    }//GEN-LAST:event_settingsActionPerformed

    private void statsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statsActionPerformed
        // TODO add your handling code here:
                       new Statistics().setVisible(true);

    }//GEN-LAST:event_statsActionPerformed

    private void addempActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addempActionPerformed
        // TODO add your handling code here:
      JFrame addEmpFrame = new JFrame("Add Employee");
    addEmpFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Dispose the frame on close
    addEmpFrame.getContentPane().add(new addemp());
    addEmpFrame.pack();
    addEmpFrame.setVisible(true);
    

    
    }//GEN-LAST:event_addempActionPerformed

    private void cameraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cameraActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cameraActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Dash.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Dash.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Dash.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Dash.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Dash().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addemp;
    private javax.swing.JComboBox<String> camera;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList<String> loglist;
    private javax.swing.JButton settings;
    private javax.swing.JButton stats;
    private javax.swing.JLabel title;
    private javax.swing.JPanel webcampanel;
    // End of variables declaration//GEN-END:variables
}

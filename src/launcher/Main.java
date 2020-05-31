package launcher;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.font.FontRenderContext;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
public class Main{
    private static final int WIDTH = 900;
    private static final int HEIGHT = 550;
    private static final int titleBarHeight = 40;
    private static final float titleHeight = .75f;//percent of title bar height;
    private static final int titleButtonSize = 4;//arbitrary value. minimum: 3
    private static final float buttonSize = .08f;//percent of content area height
    private static final float playWidth = 3f;//multiplier of height
    private static final float textSize = .04f;//percent of content area height
    private static final float tabTextHeight = .9f;//percent of content area height
    private static final float headerHeight = .1f;//percent of content area height
    private static final float buttonIndent = 1.5f;//indent from right side of screen
    private static final float playIndent = 4.5f;//indent from right side of screen
    private static final float installWidth = 12f;//multiplier of height
    private static Tab selectedTab = Tab.PLAY;
    private static int[] dragging = null;
    private static JPanel panel = new JPanel();
    private static JFrame frame;
    private static final String name = "Thizzy'z Games";
    private static final String root = System.getenv("APPDATA")+"\\"+name+"\\Launcher";
    private static final String libraryRoot = System.getenv("APPDATA")+"\\"+name+"\\Libraries";
    private static final Font font = addFont("simplelibrary-high-resolution.ttf");
    private static final int OS_WINDOWS = 0;
    private static final int OS_SOLARIS = 1;
    private static final int OS_MACOSX = 2;
    private static final int OS_LINUX = 3;
    private static int OS = -1;
    private static int whichBitDepth = -1;
    private static int BIT_32 = 0;
    private static int BIT_64 = 1;
    public static void main(String[] args){
        String OS = System.getenv("OS");
        switch(OS){
            case "Windows_NT":
                Main.OS = OS_WINDOWS;
                break;
//                Main.OS = OS_SOLARIS;
//                break;
//                Main.OS = OS_MACOSX;
//                break;
//                Main.OS = OS_LINUX;
//                break;
            default:
                Main.OS = JOptionPane.showOptionDialog(null, "Unrecognized OS \""+OS+"\"!\nPlease report this as a bug!\nIn the meantime, which OS are you currently running?", "Unrecognized Operating System", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[]{"Windows", "Solaris", "Mac OSX", "Linux"}, "Windows");
                if(Main.OS<0||Main.OS>3){
                    System.exit(0);
                }
        }
        String version = System.getenv("PROCESSOR_ARCHITECTURE");
        switch(version){
            case "x86":
                whichBitDepth = BIT_32;
                break;
            case "AMD64":
                whichBitDepth = BIT_64;
                break;
            default:
                whichBitDepth = JOptionPane.showOptionDialog(null, "Unrecognized processor architecture \""+version+"\"!\nPlease report this as a bug!.\nIn the meantime, Are you currently running 64 bit?", "Unrecognized Processor Architecture", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[]{"No, treat it as a 32 bit system", "Yes, treat it as a 64 bit system"}, "Yes, treat it as a 64 bit system");
                if(whichBitDepth<0||whichBitDepth>1){
                    System.exit(0);
                }
        }
        File launcherVersions = downloadFile("https://www.dropbox.com/s/rjdln5jt1z20zuw/versions.txt?dl=1", new File(root+"\\versions.txt"), false);
        if(launcherVersions.exists()){
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(launcherVersions)))){
                String newVersion = null;
                String[] updaterLink = reader.readLine().split("=", 3);
                String line = "";
                while((line = reader.readLine())!=null){
                    if(line.trim().isEmpty())continue;
                    if(VersionManager.getVersionID(line.split("=")[0])==-1){
                        newVersion = line;
                    }
                }
                if(newVersion!=null){
                    File newLauncher = downloadFile(newVersion.split("=", 2)[1], new File(root+"\\launcher.jar"), false);
                    File updater = downloadFile(updaterLink[2], new File(root+"\\updater "+updaterLink[1]+".jar"), true);
                    if(updater.exists()){
                        startJava(new String[0], new String[]{Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath(), newLauncher.getAbsolutePath()}, updater);
                        System.exit(0);
                    }
                }
            }catch(URISyntaxException ex){
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }catch(IOException ex){}
        }
        frame = new JFrame(name+" Launcher "+VersionManager.currentVersion);
        frame.setUndecorated(true);
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screenSize = tk.getScreenSize();
        frame.setSize(WIDTH, HEIGHT);
        frame.setLocation((screenSize.width-WIDTH)/2, (screenSize.height-HEIGHT)/2);
        frame.add(panel);
        panel.setLayout(null);
        frame.setVisible(true);
        new Game(Tab.PLAY, "Planetary Protector", "Defend the earth against otherworldly attackers!", "https://www.dropbox.com/s/capgobag47srs17/versions.txt?dl=1", "https://www.dropbox.com/s/cv045rh295rjubk/libraries.txt?dl=1");
        new Game(Tab.PLAY, "Pizza", "Make Pizza!", "https://www.dropbox.com/s/vfj58y5swhmcodj/versions.txt?dl=1", "https://www.dropbox.com/s/j9t64xx9c3y971b/libraries.txt?dl=1");
        new Game(Tab.PLAY, "Amazing Machine", "Find your way through randomly generated mazes!", "https://www.dropbox.com/s/c4yj9a8ady6zmgw/versions.txt?dl=1", "https://www.dropbox.com/s/lwzts9tcxriqkj2/libraries.txt?dl=1");
        new Game(Tab.PLAY, "Delubrian Invaders", "Explore and defend the galaxy!", "https://www.dropbox.com/s/17xbr20cq7m95oi/versions.txt?dl=1", "https://www.dropbox.com/s/t1dirgsm8ukg2cu/libraries.txt?dl=1");
        new Game(Tab.TOOLS, "Geometry Printer", "Create and print geometric shapes!", "https://www.dropbox.com/s/3wu46gic6jwn2a2/versions.txt?dl=1", "https://www.dropbox.com/s/6dos3qgxkx8xlys/libraries.txt?dl=1");
        rebuild();
    }
    private static void rebuild(){
        int width = frame.getWidth();
        int height = frame.getHeight();
        panel.setBounds(0, 0, width, height);
        panel.removeAll();
        //<editor-fold defaultstate="collapsed" desc="Title Bar">
        JPanel titleBar = new JPanel(){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(getForeground());
                int height = (int)(getHeight()*titleHeight);
                g.setFont(font.deriveFont((float)height));
                g.drawString(frame.getTitle(), 0, getHeight()/2+height/2);
            }
        };
        titleBar.setLayout(null);
        panel.add(titleBar);
        titleBar.setBounds(0,0,width,40);
        titleBar.setBackground(Backgrounds.titleBar);
        titleBar.setForeground(Color.white);
        titleBar.addMouseListener(new MouseListener(){
            @Override
            public void mouseClicked(MouseEvent e) {}
            @Override
            public void mousePressed(MouseEvent e){
                if((frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH)return;
                dragging = new int[]{e.getX(), e.getY()};
            }
            @Override
            public void mouseReleased(MouseEvent e){
                dragging = null;
            }
            @Override
            public void mouseEntered(MouseEvent e){}
            @Override
            public void mouseExited(MouseEvent e){
                if(dragging!=null){
                    frame.setLocation(e.getXOnScreen()-dragging[0], e.getYOnScreen()-dragging[1]);
                }
            }
        });
        titleBar.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if(dragging!=null){
                    frame.setLocation(e.getXOnScreen()-dragging[0], e.getYOnScreen()-dragging[1]);
                }
            }
            @Override
            public void mouseMoved(MouseEvent e) {
            }
        });
        JPanel close = new JPanel(){
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, width, height);
                g.setColor(getForeground());
                int l = getWidth()/titleButtonSize;
                int r = getWidth()*(titleButtonSize-1)/titleButtonSize;
                g.drawLine(l, l, r, r);
                g.drawLine(r, l, l, r);
            }
        };
        close.setBackground(Backgrounds.titleBar);
        close.setForeground(Color.white);
        close.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e){}
            @Override
            public void mousePressed(MouseEvent e){
                frame.dispose();
            }
            @Override
            public void mouseReleased(MouseEvent e){}
            @Override
            public void mouseEntered(MouseEvent e){
                close.setForeground(Color.red);
            }
            @Override
            public void mouseExited(MouseEvent e){
                close.setForeground(Color.white);
            }
        });
        titleBar.add(close);
        close.setBounds(titleBar.getWidth()-titleBar.getHeight(), 0, titleBar.getHeight(), titleBar.getHeight());
        JPanel maximize = new JPanel(){
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, width, height);
                g.setColor(getForeground());
                if((frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH){
                    int l = getWidth()/titleButtonSize;
                    int r = getWidth()*(titleButtonSize-1)/titleButtonSize;
                    int d = l/2;
                    g.drawLine(l, l+d, l, r);
                    g.drawLine(l, l+d, r-d, l+d);
                    g.drawLine(r-d, l+d, r-d, r);
                    g.drawLine(l, r, r-d, r);
                    g.drawLine(l+d, l, r, l);
                    g.drawLine(l+d, l, l+d, l+d);
                    g.drawLine(r, l, r, r-d);
                    g.drawLine(r-d, r-d, r, r-d);
                }else{
                    int l = getWidth()/titleButtonSize;
                    int r = getWidth()*(titleButtonSize-1)/titleButtonSize;
                    g.drawLine(l, l, r, l);
                    g.drawLine(r, l, r, r);
                    g.drawLine(l, r, r, r);
                    g.drawLine(l, l, l, r);
                }
            }
        };
        maximize.setBackground(Backgrounds.titleBar);
        maximize.setForeground(Color.lightGray);
        maximize.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e){}
            @Override
            public void mousePressed(MouseEvent e){
                if((frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) frame.setExtendedState(JFrame.NORMAL);
                else frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                rebuild();
            }
            @Override
            public void mouseReleased(MouseEvent e){}
            @Override
            public void mouseEntered(MouseEvent e){
                maximize.setForeground(Color.white);
            }
            @Override
            public void mouseExited(MouseEvent e){
                maximize.setForeground(Color.lightGray);
            }
        });
        titleBar.add(maximize);
        maximize.setBounds(titleBar.getWidth()-titleBar.getHeight()*2, 0, titleBar.getHeight(), titleBar.getHeight());
        JPanel minimize = new JPanel(){
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, width, height);
                g.setColor(getForeground());
                int l = getWidth()/titleButtonSize;
                int r = getWidth()*(titleButtonSize-1)/titleButtonSize;
                g.drawLine(l, r, r, r);
            }
        };
        minimize.setBackground(Backgrounds.titleBar);
        minimize.setForeground(Color.lightGray);
        minimize.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e){}
            @Override
            public void mousePressed(MouseEvent e){
                frame.setExtendedState(frame.getExtendedState() | JFrame.ICONIFIED);
            }
            @Override
            public void mouseReleased(MouseEvent e){}
            @Override
            public void mouseEntered(MouseEvent e){
                minimize.setForeground(Color.white);
            }
            @Override
            public void mouseExited(MouseEvent e){
                minimize.setForeground(Color.lightGray);
            }
        });
        titleBar.add(minimize);
        minimize.setBounds(titleBar.getWidth()-titleBar.getHeight()*3, 0, titleBar.getHeight(), titleBar.getHeight());
//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Content Panel">
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(null);
        panel.add(contentPanel);
        contentPanel.setBounds(0,titleBarHeight,width,height-titleBarHeight);
        contentPanel.setBackground(Backgrounds.contentPanel);
        //<editor-fold defaultstate="collapsed" desc="Header">
        JPanel header = new JPanel();
        header.setLayout(null);
        contentPanel.add(header);
        header.setBounds(0, 0, contentPanel.getWidth(), (int) (headerHeight*contentPanel.getHeight()));
        header.setBackground(Backgrounds.header);
        //<editor-fold defaultstate="collapsed" desc="Header Buttons">
        Tab[] tabs = Tab.values();
        int tabWidth = header.getWidth()/tabs.length;
        for(int i = 0; i<tabs.length; i++){
            Tab tab = tabs[i];
            JPanel button = new JPanel(){
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.setColor(getBackground());
                    g.fillRect(0, 0, getWidth(), getHeight());
                    BufferedImage image = getImage("tabs/"+tab.name().toLowerCase()+".png");
                    if(image!=null){
                        int adjustedWidth = image.getWidth()*getHeight()/image.getHeight();
                        g.drawImage(image, getWidth()/2-adjustedWidth/2, 0, adjustedWidth, getHeight(), null);
                    }
                    g.setColor(getForeground());
                    int height = (int)(getHeight()*tabTextHeight);
                    Font f = font.deriveFont((float)height);
                    int width = (int) f.getStringBounds(tab.toString(), new FontRenderContext(null, false, false)).getWidth();
                    g.setFont(f);
                    g.drawString(tab.toString(), getWidth()/2-width/2, getHeight()/2+height/2);
                }
            };
            header.add(button);
            if(i==tabs.length-1){
                button.setBounds(tabWidth*i, 0, header.getWidth()-(tabWidth*(tabs.length-1)), header.getHeight());
            }else{
                button.setBounds(tabWidth*i, 0, tabWidth, header.getHeight());
            }
            button.setBackground(header.getBackground());
            if(selectedTab==tab)button.setBackground(Backgrounds.selectedTab);
            button.setForeground(Color.white);
            button.addMouseListener(new MouseListener(){
                @Override
                public void mouseClicked(MouseEvent e){}
                @Override
                public void mousePressed(MouseEvent e){
                    selectedTab = tab;
                    rebuild();
                }
                @Override
                public void mouseReleased(MouseEvent e){}
                @Override
                public void mouseEntered(MouseEvent e){
                    if(selectedTab!=tab){
                        button.setBackground(Backgrounds.mouseoverTab);
                    }
                }
                @Override
                public void mouseExited(MouseEvent e){
                    if(selectedTab!=tab){
                        button.setBackground(Backgrounds.header);
                    }
                }
            });
        }
        //</editor-fold>
        //</editor-fold>
        if(selectedTab==Tab.SETTINGS){
            JPanel installDir = new JPanel(){
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.setColor(getBackground());
                    g.fillRect(0, 0, getWidth(), getHeight());
                    BufferedImage image;
                    if(getForeground()==Color.white)image = getImage("icons/installMouseover.png");
                    else image = getImage("icons/install.png");
                    g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
                    g.setColor(getForeground());
                    int height = (int) (buttonSize*contentPanel.getHeight()*.9f);
                    Font f = font.deriveFont((float)height);
                    int width = (int) f.getStringBounds("Open Install Directory", new FontRenderContext(null, false, false)).getWidth();
                    g.setFont(f);
                    g.drawString("Open Install Directory", getWidth()/2-width/2, getHeight()/2+height/2);
                }
            };
            contentPanel.add(installDir);
            int size = (int) (contentPanel.getHeight()*buttonSize);
            installDir.setBounds((int) (contentPanel.getWidth()-size*(playIndent+installWidth)), contentPanel.getHeight()/2-size/2, (int) (size*installWidth), size);
            installDir.setBackground(Backgrounds.contentPanel);
            installDir.setForeground(new Color(245,245,245));
            installDir.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e){}
                @Override
                public void mousePressed(MouseEvent e){
                    try{
                        Desktop.getDesktop().open(new File(System.getenv("APPDATA")+"\\"+name));
                    }catch(IOException ex){
                    }
                }
                @Override
                public void mouseReleased(MouseEvent e){}
                @Override
                public void mouseEntered(MouseEvent e){
                    installDir.setForeground(Color.white);
                }
                @Override
                public void mouseExited(MouseEvent e){
                    installDir.setForeground(new Color(245,245,245));
                }
            });
        }else{
            //<editor-fold defaultstate="collapsed" desc="Game List">
            if(!selectedTab.games.isEmpty()){
                int gameWidth = width/4;
                int gameHeight = (contentPanel.getHeight()-header.getHeight())/selectedTab.games.size();
                for(int i = 0; i<selectedTab.games.size(); i++){
                    Game game = selectedTab.games.get(i);
                    //<editor-fold defaultstate="collapsed" desc="Game Title">
                    JPanel gameTitle = new JPanel(){
                        @Override
                        protected void paintComponent(Graphics g){
                            super.paintComponent(g);
                            g.setColor(getBackground());
                            g.fillRect(0, 0, getWidth(), getHeight());
                            BufferedImage image = getImage("games/"+game.name.toLowerCase().replace(" ", "_")+".png");
                            if(image!=null){
                                int adjustedWidth = image.getWidth()*getHeight()/image.getHeight();
                                g.drawImage(image, getWidth()/2-adjustedWidth/2, 0, adjustedWidth, getHeight(), null);
                            }
                            g.setColor(getForeground());
                            int height = (int) (textSize*contentPanel.getHeight());
                            Font f = font.deriveFont((float)height);
                            int width = (int) f.getStringBounds(game.name, new FontRenderContext(null, false, false)).getWidth();
                            g.setFont(f);
                            g.drawString(game.name, getWidth()/2-width/2, getHeight()/2+height/2);
                            if(game.currentVersion!=null){
                                width = (int) f.getStringBounds(game.currentVersion, new FontRenderContext(null, false, false)).getWidth();
                                g.drawString(game.currentVersion, getWidth()/2-width/2, getHeight()/2+height*3/2);
                            }
                        }
                    };
                    contentPanel.add(gameTitle);
                    if(i==selectedTab.games.size()-1){
                        gameTitle.setBounds(0, header.getHeight()+gameHeight*i, gameWidth, contentPanel.getHeight()-header.getHeight()-(gameHeight*(selectedTab.games.size()-1)));
                    }else{
                        gameTitle.setBounds(0, header.getHeight()+gameHeight*i, gameWidth, gameHeight);
                    }
                    gameTitle.setBackground(Backgrounds.game);
                    gameTitle.setForeground(Color.white);
                    //</editor-fold>
                    if(game.getStatus()==Status.NOT_INSTALLED){
                            JPanel download = new JPanel(){
                                @Override
                                protected void paintComponent(Graphics g) {
                                    super.paintComponent(g);
                                    g.setColor(getBackground());
                                    g.fillRect(0, 0, getWidth(), getHeight());
                                    BufferedImage image;
                                    if(getForeground()==Color.white)image = getImage("icons/downloadMouseover.png");
                                    else image = getImage("icons/download.png");
                                    g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
                                }
                            };
                            contentPanel.add(download);
                            int size = (int) (contentPanel.getHeight()*buttonSize);
                            download.setBounds((int) (contentPanel.getWidth()-size*(buttonIndent+1)), gameTitle.getY()+gameTitle.getHeight()/2-size/2, size, size);
                            download.setBackground(Backgrounds.contentPanel);
                            download.setForeground(Color.lightGray);
                            download.addMouseListener(new MouseListener() {
                            @Override
                            public void mouseClicked(MouseEvent e){}
                            @Override
                            public void mousePressed(MouseEvent e){
                                game.download();
                            }
                            @Override
                            public void mouseReleased(MouseEvent e){}
                            @Override
                            public void mouseEntered(MouseEvent e){
                                download.setForeground(Color.white);
                            }
                            @Override
                            public void mouseExited(MouseEvent e){
                                download.setForeground(Color.lightGray);
                            }
                        });
                    }else{
                        if(game.getStatus()==Status.DOWNLOADED){
                                JPanel play = new JPanel(){
                                    @Override
                                    protected void paintComponent(Graphics g) {
                                        super.paintComponent(g);
                                        g.setColor(getBackground());
                                        g.fillRect(0, 0, getWidth(), getHeight());
                                        BufferedImage image;
                                        if(getForeground()==Color.white)image = getImage("icons/playMouseover.png");
                                        else image = getImage("icons/play.png");
                                        g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
                                        g.setColor(getForeground());
                                        int height = (int) (buttonSize*contentPanel.getHeight()*.9f);
                                        Font f = font.deriveFont((float)height);
                                        int width = (int) f.getStringBounds("Play", new FontRenderContext(null, false, false)).getWidth();
                                        g.setFont(f);
                                        g.drawString("Play", getWidth()/2-width/2, getHeight()/2+height/2);
                                    }
                                };
                                contentPanel.add(play);
                                int size = (int) (contentPanel.getHeight()*buttonSize);
                                play.setBounds((int) (contentPanel.getWidth()-size*(playIndent+playWidth)), gameTitle.getY()+gameTitle.getHeight()/2-size/2, (int) (size*playWidth), size);
                                play.setBackground(Backgrounds.contentPanel);
                                play.setForeground(new Color(245,245,245));
                                play.addMouseListener(new MouseListener() {
                                    @Override
                                    public void mouseClicked(MouseEvent e){}
                                    @Override
                                    public void mousePressed(MouseEvent e){
                                        game.play();
                                    }
                                    @Override
                                    public void mouseReleased(MouseEvent e){}
                                    @Override
                                    public void mouseEntered(MouseEvent e){
                                        play.setForeground(Color.white);
                                    }
                                    @Override
                                    public void mouseExited(MouseEvent e){
                                        play.setForeground(new Color(245,245,245));
                                    }
                                });
                            if(game.hasUpdate()){
                                JPanel update = new JPanel(){
                                    @Override
                                    protected void paintComponent(Graphics g) {
                                        super.paintComponent(g);
                                        g.setColor(getBackground());
                                        g.fillRect(0, 0, getWidth(), getHeight());
                                        BufferedImage image;
                                        if(getForeground()==Color.white)image = getImage("icons/updateMouseover.png");
                                        else image = getImage("icons/update.png");
                                        g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
                                    }
                                };
                                contentPanel.add(update);
                                size = (int) (contentPanel.getHeight()*buttonSize);
                                update.setBounds((int) (contentPanel.getWidth()-size*(buttonIndent+1)), gameTitle.getY()+gameTitle.getHeight()/2-size/2, size, size);
                                update.setBackground(Backgrounds.contentPanel);
                                update.setForeground(Color.lightGray);
                                update.addMouseListener(new MouseListener() {
                                    @Override
                                    public void mouseClicked(MouseEvent e){}
                                    @Override
                                    public void mousePressed(MouseEvent e){
                                        game.update();
                                    }
                                    @Override
                                    public void mouseReleased(MouseEvent e){}
                                    @Override
                                    public void mouseEntered(MouseEvent e){
                                        update.setForeground(Color.white);
                                    }
                                    @Override
                                    public void mouseExited(MouseEvent e){
                                        update.setForeground(Color.lightGray);
                                    }
                                });
                            }
                        }
                    }
                }
            }
//</editor-fold>
        }
        //</editor-fold>
        frame.repaint();
    }
    private static class Backgrounds{
        private static final Color contentPanel = new Color(130,130,130);
        private static final Color titleBar = new Color(90,90,90);
        private static final Color header = new Color(110,111,112);
        private static final Color selectedTab = new Color(100, 101, 102);
        private static final Color mouseoverTab = new Color(120, 121, 122);
        private static final Color game = new Color(120, 120, 120);
//        private static final Color selectedGame = new Color(120, 121, 122);
//        private static final Color mouseoverGame = new Color(140, 141, 142);
    }
    private static enum Tab{
        PLAY,TOOLS,SETTINGS;
        private final ArrayList<Game> games = new ArrayList<>();
        @Override
        public String toString(){
            return name().charAt(0)+name().substring(1).toLowerCase();
        }
    }
    private static final HashMap<String, BufferedImage> images = new HashMap<>();
    private static BufferedImage getImage(String path){
        if(images.containsKey(path))return images.get(path);
        try {
            if(new File("nbproject").exists()){
                images.put(path, ImageIO.read(new File("src\\textures\\"+path.replace("/", "\\"))));
            }else{
                JarFile jar = new JarFile(new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath().replace("%20", " ")));
                Enumeration enumEntries = jar.entries();
                while(enumEntries.hasMoreElements()){
                    JarEntry file = (JarEntry)enumEntries.nextElement();
                    System.out.println(file.getName());
                    if(file.getName().equals("textures/"+path.replace("\\", "/"))){
                        images.put(path, ImageIO.read(jar.getInputStream(file)));
                        break;
                    }
                }
            }
        } catch (IOException ex) {
            System.err.println("Image not found: "+path);
            images.put(path, null);
        }
        return images.get(path);
    }
    private static Font addFont(String path){
        try {
            if(new File("nbproject").exists()){
                return Font.createFont(Font.TRUETYPE_FONT, new File("src\\fonts\\"+path.replace("/", "\\")));
            }else{
                JarFile jar = new JarFile(new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath().replace("%20", " ")));
                Enumeration enumEntries = jar.entries();
                while(enumEntries.hasMoreElements()){
                    JarEntry file = (JarEntry)enumEntries.nextElement();
                    System.out.println(file.getName());
                    if(file.getName().equals("fonts/"+path.replace("\\", "/"))){
                        return Font.createFont(Font.TRUETYPE_FONT, jar.getInputStream(file));
                    }
                }
            }
        } catch (IOException | FontFormatException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    private static class Game{
        private final String name;
        private final String gameRoot;
        private Status status;
        private final String description;
        private final String versionsURL;
        private final String librariesURL;
        private final ArrayList<String> versions = new ArrayList<>();
        private final ArrayList<String> libraries = new ArrayList<>();
        private String currentVersion;
        private Game(Tab tab, String name, String description, String versionsURL, String librariesURL){
            this.name = name;
            gameRoot = root+"\\"+name;
            status = getJarfile().exists()?Status.DOWNLOADED:null;
            this.description = description;
            this.versionsURL = versionsURL;
            this.librariesURL = librariesURL;
            tab.games.add(this);
            File version = new File(gameRoot+"\\version.txt");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(version)))) {
                currentVersion = reader.readLine();
            }catch(IOException ex){
            }
            new Thread(()->{
                File versionsFile = downloadFile(versionsURL, new File(gameRoot+"\\versions.txt"), false);
                File librariesFile = downloadFile(librariesURL, new File(gameRoot+"\\libraries.txt"), false);
                if(versionsFile!=null){
                    try{
                        BufferedReader versions = new BufferedReader(new InputStreamReader(new FileInputStream(versionsFile)));
                        String line;
                        while((line = versions.readLine())!=null){
                            if(!line.isEmpty())this.versions.add(line);
                        }
                        versions.close();
                    }catch(IOException ex){
                        versionsFile = null;
                    }
                }
                if((versionsFile!=null&&librariesFile!=null&&!versions.isEmpty())||status==Status.DOWNLOADED){
                    if(status==null)status = Status.NOT_INSTALLED;
                    rebuild();
                }
            }, name+" Discovery Thread").start();
        }
        private Status getStatus(){
            return status;
        }
        private boolean hasUpdate(){
            if(currentVersion==null)return false;
            if(versions.isEmpty())return false;
            return !versions.get(versions.size()-1).startsWith(currentVersion+"=");
        }
        private void download(){
            Thread t = new Thread(() -> {
                status = Status.DOWNLOADING;
                rebuild();
                downloadFile(versions.get(versions.size()-1).split("=", 2)[1], getJarfile(), false);
                File version = new File(gameRoot+"\\version.txt");
                if(version.exists())version.delete();
                version.getParentFile().mkdirs();
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(version)))) {
                    String ver = versions.get(versions.size()-1).split("=")[0];
                    writer.write(ver);
                    currentVersion = ver;
                }catch(IOException ex){
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    File currentLibraries = new File(gameRoot+"\\currentLibraries.txt");
                    currentLibraries.delete();
                    Files.copy(new File(gameRoot+"\\libraries.txt").toPath(), currentLibraries.toPath());
                    if(!verifyLibraries()){
                        JOptionPane.showMessageDialog(null, "Failed to verify dependencies!", "Download Failed", JOptionPane.OK_OPTION);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
                status = Status.DOWNLOADED;
                rebuild();
            }, name+" Download Thread");
            t.start();
        }
        private void update(){
            download();
        }
        private File getJarfile(){
            return new File(gameRoot+"\\"+name+".jar");
        }
        private void findLibraries(){
            try{
                if(libraries.isEmpty()){
                    File currentLibraries = new File(gameRoot+"\\currentLibraries.txt");
                    BufferedReader libraries = new BufferedReader(new InputStreamReader(new FileInputStream(currentLibraries)));
                    String line;
                    while((line = libraries.readLine())!=null){
                        if(!line.isEmpty())this.libraries.add(line);
                    }
                    libraries.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        private boolean verifyLibraries(){
            return verifyLibraries(null);
        }
        private boolean verifyLibraries(ArrayList<File> files){
            if(files==null)files = new ArrayList<>();
            findLibraries();
            for(String lib : libraries){
                lib = lib.trim();
                if(lib.isEmpty())continue;
                if(lib.equalsIgnoreCase("LWJGL")){
                    String[][] nativesPaths = {
                        {"https://dl.dropboxusercontent.com/s/1nt1g7ui7p4eb54/windows32natives.zip?dl=1&token_hash=AAFsOnqBqipIOxc4sNr138FnlZIjHBf-KPwMTNe8F5lqOQ",
                         "https://dl.dropboxusercontent.com/s/y41peavuls3ptzu/windows64natives.zip?dl=1&token_hash=AAEJ6Ih8HGEsla1tJmIB7R-YBCTC8LVq_D4OFcFWDCEZ5Q"},
                        {"https://dl.dropboxusercontent.com/s/h4z3j2pspuos15l/solaris32natives.zip?dl=1&token_hash=AAEmlE84CzHRTqya3xPN9xRh_1_v0nGccJFp-bfru4jSRw",
                         "https://dl.dropboxusercontent.com/s/vq3x3n81x0qvc3u/solaris64natives.zip?dl=1&token_hash=AAEyl6swuFIukpTNZjrgv96TGwSnMYxWt0hdQ71_KiqQqw"},
                        {"https://dl.dropboxusercontent.com/s/ljvgoccqz33bcq1/macosx32natives.zip?dl=1&token_hash=AAGezz3pNxqa6Fi_O-xGCZdI2923D7b-ZsrWZ61HlFROYw",
                         null},
                        {"https://dl.dropboxusercontent.com/s/nfv4ra6n68lna9n/linux32natives.zip?dl=1&token_hash=AAGzHZLGp9S4HAjzpzNZp9-YixYw4H56D6_DJ3dG5GDeFA",
                         "https://dl.dropboxusercontent.com/s/rp6uhdmec7697ty/linux64natives.zip?dl=1&token_hash=AAHl6tcg11VwWr31WtqMUlozabCSpr0LfS5MLS2MpmWnEA"}
                    };
                    String[] osPaths = nativesPaths[OS];
                    if(!downloadLibrary("https://dl.dropboxusercontent.com/s/p7v72lix4gl96co/lwjgl.jar?dl=1&token_hash=AAG5TMAYw0Oq1_xwgVjKoE8FkKXMaWOfpj5cau1UuWKZlA", files))return false;
                    if(!downloadLibrary("https://dl.dropboxusercontent.com/s/9ylaq5w5vzj1lgi/jinput.jar?dl=1&token_hash=AAHILxU3uc-UU5vXj7N4i5s1huBKYSzKGgKq3MawNJB05w", files))return false;
                    if(!downloadLibrary("https://dl.dropboxusercontent.com/s/fog6w5pcxqf4zd9/lwjgl_util.jar?dl=1&token_hash=AAHwYq0uL4zeuTrLoi8EiG_RiUeMDZDsnlm4KYNScpy0Sw", files))return false;
                    if(!downloadLibrary("https://dl.dropboxusercontent.com/s/60en1x8in11leqn/lzma.jar?dl=1&token_hash=AAGUFJwmD9jKmk7j4M53Xr0_6Sisf5RSRW3JAjRgsml4gg", files))return false;
                    File bit32 = downloadFile(osPaths[BIT_32], new File(libraryRoot+"\\natives32.zip"), true);
                    File bit64 = whichBitDepth==BIT_64?downloadFile(osPaths[BIT_64], new File(libraryRoot+"\\natives64.zip"), true):null;
                    File nativesDir = new File(libraryRoot+"\\natives");
                    if(bit32==null||(whichBitDepth==BIT_64&&bit64==null&&osPaths[BIT_64]!=null))return false;
                    if(!nativesDir.exists()){
                        extractFile(bit32, nativesDir);
                        if(bit64!=null){
                            extractFile(bit64, nativesDir);
                        }
                    }
                }else if(lib.toLowerCase().startsWith("simplibext")){
                    String version = lib.substring(10).trim();
                    try{
                        File simpLibExtendedVersions = downloadFile("https://www.dropbox.com/s/7k4ri81to8hc9n2/versions.dat?dl=1", new File(libraryRoot+"\\simplibext.versions"), false);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(simpLibExtendedVersions)));
                        ArrayList<String> versions = new ArrayList<>();
                        HashMap<String, String> simpLibExtended = new HashMap<>();
                        String line;
                        while((line = reader.readLine())!=null){
                            if(line.isEmpty())continue;
                            versions.add(line.split("=", 2)[0]);
                            simpLibExtended.put(line.split("=", 2)[0], line.split("=", 2)[1]);
                        }
                        reader.close();
                        if(!versions.contains(version)){
                            System.err.println("Unknown Simplelibrary_extended version "+version+"! Downloading latest version");
                            version = versions.get(versions.size()-1);
                        }
                        File f = downloadFile(simpLibExtended.get(version), new File(libraryRoot+"\\Simplelibrary_extended "+version+".jar"), true);
                        files.add(f);
                        if(!f.exists())return false;
                        simpLibExtendedVersions.delete();
                    }catch(IOException ex){
                        File f = new File(libraryRoot+"\\Simplelibrary_extended "+version+".jar");
                        files.add(f);
                        if(!f.exists())return false;
                    }
                }else if(lib.toLowerCase().startsWith("simplib")){
                    String version = lib.substring(7).trim();
                    try{
                        File simplibVersions = downloadFile("https://www.dropbox.com/s/as5y1ik7gb8gp6k/versions.dat?dl=1", new File(libraryRoot+"\\simplib.versions"), false);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(simplibVersions)));
                        ArrayList<String> versions = new ArrayList<>();
                        HashMap<String, String> simplib = new HashMap<>();
                        String line;
                        while((line = reader.readLine())!=null){
                            if(line.isEmpty())continue;
                            versions.add(line.split("=", 2)[0]);
                            simplib.put(line.split("=", 2)[0], line.split("=", 2)[1]);
                        }
                        reader.close();
                        if(!versions.contains(version)){
                            System.err.println("Unknown simplelibrary version "+version+"! Downloading latest version");
                            version = versions.get(versions.size()-1);
                        }
                        File f = downloadFile(simplib.get(version), new File(libraryRoot+"\\Simplelibrary "+version+".jar"), true);
                        files.add(f);
                        if(!f.exists())return false;
                        simplibVersions.delete();
                    }catch(IOException ex){
                        File f = new File(libraryRoot+"\\Simplelibrary "+version+".jar");
                        files.add(f);
                        if(!f.exists())return false;
                    }
                }else if(lib.endsWith("?dl=1")){
                    if(!downloadLibrary(lib, files))return false;
                }
            }
            return true;
        }
        private boolean downloadLibrary(String link, ArrayList<File> files){
            String filename = link.split("\\Q?")[0];
            while(filename.contains("/"))filename = filename.substring(filename.indexOf("/")+1);
            File f = downloadFile(link, new File(libraryRoot+"\\"+filename), true);
            files.add(f);
            return f.exists();
        }
        private void play(){
            new Thread(() -> {
                status = Status.PLAYING;
                rebuild();
                ArrayList<File> files = new ArrayList<>();
                if(!verifyLibraries(files)){
                    JOptionPane.showMessageDialog(null, "Failed to verify dependencies!", "Startup Failed", JOptionPane.OK_OPTION);
                    status = Status.DOWNLOADED;
                    rebuild();
                    return;
                }
                String[] additionalClasspathElements = new String[files.size()];
                for(int i = 0; i<files.size(); i++){
                    additionalClasspathElements[i] = files.get(i).getAbsolutePath();
                }
                ArrayList<String> theargs = new ArrayList<>();
                theargs.add(0, "Skip Dependencies");
                boolean lwjgl = false;
                ArrayList<String> vmArgs = new ArrayList<>();
                for(String lib : libraries){
                    if(lib.equalsIgnoreCase("lwjgl"))vmArgs.add("-Djava.library.path="+new File(libraryRoot+"\\natives").getAbsolutePath());
                }try{
                    final Process p = run(vmArgs.toArray(new String[vmArgs.size()]), theargs.toArray(new String[theargs.size()]), additionalClasspathElements, getJarfile());
                    new Thread(){
                        public void run(){
                            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                            String line;
                            try{
                                while((line=in.readLine())!=null){
                                    System.out.println(line);
                                }
                            }catch(IOException ex){
                                throw new RuntimeException(ex);
                            }
                        }
                    }.start();
                    new Thread(){
                        public void run(){
                            BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                            String line;
                            try{
                                while((line=in.readLine())!=null){
                                    System.err.println(line);
                                }
                            }catch(IOException ex){
                                throw new RuntimeException(ex);
                            }
                        }
                    }.start();
                    Thread t = new Thread(){
                        public void run(){
                            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                            PrintWriter out = new PrintWriter(p.getOutputStream());
                            String line;
                            try{
                                while((line=in.readLine())!=null){
                                    out.println(line);
                                    out.flush();
                                }
                            }catch(IOException ex){
                                throw new RuntimeException(ex);
                            }
                        }
                    };
                    t.setDaemon(true);
                    t.start();
                }catch(URISyntaxException | IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
                status = Status.DOWNLOADED;
                rebuild();
            }, "Startup Thread").start();
        }
    }
    private static enum Status{
        DOWNLOADING,DOWNLOADED,NOT_INSTALLED,PLAYING;
    }
    private static File downloadFile(String link, File destinationFile, boolean keep){
        if(true){
            synchronized(link){
                destinationFile.getParentFile().mkdirs();
                if(keep&&destinationFile.exists())return destinationFile;
                try {
                    Files.copy(new URL(link).openStream(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    return destinationFile;
                }
                return destinationFile;
            }
        }
        if(destinationFile.exists()&&keep)return destinationFile;
        if(link==null){
            return destinationFile;
        }
        File old = new File(destinationFile.getParentFile(), destinationFile.getName()+".old");
        if(old.exists())old.delete();
        if(destinationFile.exists()){
            destinationFile.renameTo(old);
            destinationFile.delete();
        }
        destinationFile.getParentFile().mkdirs();
        try {
            URL url = new URL(link);
            int fileSize;
            URLConnection connection = url.openConnection();
            connection.setDefaultUseCaches(false);
            if ((connection instanceof HttpURLConnection)) {
                ((HttpURLConnection)connection).setRequestMethod("HEAD");
                int code = ((HttpURLConnection)connection).getResponseCode();
                if (code / 100 == 3) {
                    if(old.exists()){
                        if(destinationFile.exists())destinationFile.delete();
                        if(old.renameTo(destinationFile)){
                            old.delete();
                            return destinationFile;
                        }
                        old.delete();
                    }
                    return null;
                }
            }
            fileSize = connection.getContentLength();
            byte[] buffer = new byte[65535];
            int unsuccessfulAttempts = 0;
            int maxUnsuccessfulAttempts = 3;
            boolean downloadFile = true;
            while (downloadFile) {
                downloadFile = false;
                URLConnection urlconnection = url.openConnection();
                if ((urlconnection instanceof HttpURLConnection)) {
                    urlconnection.setRequestProperty("Cache-Control", "no-cache");
                    urlconnection.connect();
                }
                String targetFile = destinationFile.getName();
                FileOutputStream fos;
                int downloadedFileSize;
                try (InputStream inputstream=getRemoteInputStream(targetFile, urlconnection)) {
                    fos=new FileOutputStream(destinationFile);
                    downloadedFileSize=0;
                    int read;
                    while ((read = inputstream.read(buffer)) != -1) {
                        fos.write(buffer, 0, read);
                        downloadedFileSize += read;
                    }
                }
                fos.close();
                if (((urlconnection instanceof HttpURLConnection)) && 
                    ((downloadedFileSize != fileSize) && (fileSize > 0))){
                    unsuccessfulAttempts++;
                    if (unsuccessfulAttempts < maxUnsuccessfulAttempts){
                        downloadFile = true;
                    }else{
                        throw new Exception("failed to download "+targetFile);
                    }
                }
            }
            return destinationFile;
        }catch (Exception ex){
            if(old.exists()){
                if(destinationFile.exists())destinationFile.delete();
                if(old.renameTo(destinationFile)){
                    old.delete();
                    return destinationFile;
                }
                old.delete();
            }
            return null;
        }
    }
    private static InputStream getRemoteInputStream(String currentFile, final URLConnection urlconnection) throws Exception {
        final InputStream[] is = new InputStream[1];
        for (int j = 0; (j < 3) && (is[0] == null); j++) {
            Thread t = new Thread() {
                public void run() {
                    try {
                        is[0] = urlconnection.getInputStream();
                    }catch (IOException localIOException){}
                }
            };
            t.setName("FileDownloadStreamThread");
            t.start();
            int iterationCount = 0;
            while ((is[0] == null) && (iterationCount++ < 5)){
                try {
                    t.join(1000L);
                } catch (InterruptedException localInterruptedException) {
                }
            }
            if (is[0] != null){
                continue;
            }
            try {
                t.interrupt();
                t.join();
            } catch (InterruptedException localInterruptedException1) {
            }
        }
        if (is[0] == null) {
            throw new Exception("Unable to download "+currentFile);
        }
        return is[0];
    }
    private static synchronized void extractFile(File fromZip, File toDir){
        if(!fromZip.exists()){
            return;
        }
        toDir.mkdirs();
        try(ZipInputStream in = new ZipInputStream(new FileInputStream(fromZip))){
            ZipEntry entry;
            while((entry = in.getNextEntry())!=null){
                File destFile = new File(toDir.getAbsolutePath()+"\\"+entry.getName().replaceAll("/", "\\"));
                delete(destFile);
                try(FileOutputStream out = new FileOutputStream(destFile)){
                    byte[] buffer = new byte[1024];
                    int read = 0;
                    while((read=in.read(buffer))>=0){
                        out.write(buffer, 0, read);
                    }
                }
            }
        }catch(FileNotFoundException ex){
            throw new UnsupportedOperationException(ex);
        }catch(IOException ex){
            throw new RuntimeException(ex);
        }
    }
    private static void delete(File file){
        if(!file.exists()){
            return;
        }
        if(file.isDirectory()){
            File[] files = file.listFiles();
            if(files!=null){
                for(File afile : files){
                    delete(afile);
                }
            }
        }
        file.delete();
    }
    /**
     * Restarts the program.  This method will return normally if the program was properly restarted or throw an exception if it could not be restarted.
     * @param vmArgs The VM arguments for the new instance
     * @param applicationArgs The application arguments for the new instance
     * @param additionalFiles Any additional files to include in the classpath
     * @param jarfile The Jar file to run
     * @param mainClass The program's main class.  The new instance is started with this as the specified main class.
     * @throws URISyntaxException if a URI cannot be created to obtain the filepath to the jarfile
     * @throws IOException if Java is not in the PATH environment variable
     */
    private static Process run(String[] vmArgs, String[] applicationArgs, String[] additionalFiles, File jarfile) throws URISyntaxException, IOException{
        ArrayList<String> params = new ArrayList<>();
        params.add("java");
        params.addAll(Arrays.asList(vmArgs));
        params.add("-cp");
        String filepath = jarfile.getAbsolutePath();
        for(String str : additionalFiles){
            filepath+=";"+str;
        }
        params.add(filepath);
        params.add(getMainClass(jarfile));
        params.addAll(Arrays.asList(applicationArgs));
        System.out.println(params);
        ProcessBuilder builder = new ProcessBuilder(params);
        return builder.start();
    }
    private static String getMainClass(File f){
        try(ZipFile j = new ZipFile(f);BufferedReader in = new BufferedReader(new InputStreamReader(j.getInputStream(j.getEntry("META-INF/MANIFEST.MF"))))){
            ArrayList<String> n = new ArrayList<>();
            String g = null;
            while((g=in.readLine())!=null){
                if(g.startsWith("Main-Class:")){
                    return g.split("\\Q:",2)[1].trim();
                }
            }
        }catch(Throwable t){
            throw new RuntimeException(t);
        }
        return null;
    }
    /**
     * Starts the requested Java application the program.  This method will return normally if the program was properly restarted or throw an exception if it could not be restarted.
     * @param vmArgs The VM arguments for the new instance
     * @param applicationArgs The application arguments for the new instance
     * @param file The program file.  The new instance is started with this as the specified main class.
     * @throws URISyntaxException if a URI cannot be created to obtain the filepath to the jarfile
     * @throws IOException if Java is not in the PATH environment variable
     */
    private static Process startJava(String[] vmArgs, String[] applicationArgs, File file) throws URISyntaxException, IOException{
        ArrayList<String> params = new ArrayList<>();
        params.add("java");
        params.addAll(Arrays.asList(vmArgs));
        params.add("-jar");
        params.add(file.getAbsolutePath());
        params.addAll(Arrays.asList(applicationArgs));
        ProcessBuilder builder = new ProcessBuilder(params);
        return builder.start();
    }
}
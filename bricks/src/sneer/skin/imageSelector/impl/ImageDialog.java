package sneer.skin.imageSelector.impl;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import sneer.skin.image.ImageFactory;

public class ImageDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private ImageFactory _imageFactory;

	private File _file;
	private Picture _picture;
	private AvatarPreview _avatarPreview;
	private JLayeredPane _layeredPane;

	private int _preferredHeight;
	private int _preferredWidth;

    public ImageDialog(File file, ImageFactory imageFactory) {
    	_file = file;
    	_imageFactory = imageFactory;
		_avatarPreview = new AvatarPreview(this);
		_picture = new Picture(_avatarPreview);
		
		initWindow();
		initLayers();
		addImageInLayer();
		addSizeListener();
		
		SwingUtilities.invokeLater(
			new Runnable() {
				@Override
				public void run() {
					_picture.setLocation(0, 0);
					setVisible(true);
					System.out.println(_picture.getBounds());
				}
			}
		);		
	}

	private void initWindow() {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		Dimension desktopSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		_preferredHeight = (int) (desktopSize.height*0.8);
		_preferredWidth = (int) (desktopSize.width*0.8);

		setBounds((desktopSize.width-_preferredWidth)/2,
				(desktopSize.height-_preferredHeight)/2,
				_preferredWidth,_preferredHeight);
	}

	private void initLayers() {
		_layeredPane = new JLayeredPane();
		getContentPane().add(_layeredPane);
	}

    private void addImageInLayer() {
		ImageIcon icon = getIcon(_file, _preferredHeight, _preferredWidth);		
		_picture.setIcon(icon);
		_layeredPane.setLayout(new FlowLayout());    
        _layeredPane.add(_picture, JLayeredPane.DEFAULT_LAYER);
        Keyhole keyhole = new Keyhole(_layeredPane);
    	_layeredPane.add(keyhole, JLayeredPane.POPUP_LAYER);
    	
    	//resize window
		setSize(new Dimension(icon.getIconWidth(),icon.getIconHeight()));
    }
    
	private void addSizeListener() {
		Toolkit.getDefaultToolkit().setDynamicLayout(true);
		getContentPane().addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				_picture.setVisible(false);
				_picture.setIcon(getIcon(_file, getHeight(), getWidth()));
				_avatarPreview.resizeAvatarPreview();
				SwingUtilities.invokeLater(
					new Runnable() {
						@Override
						public void run() {
							_picture.setVisible(true);
							_avatarPreview.setVisible(true);
						}
					}
				);
			}
			
		});
		
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(ComponentEvent e) {
				_avatarPreview.resizeAvatarPreview();
			}
		});
	}
	
	private ImageIcon getIcon(final File file, int height, int width) {
		ImageIcon icon = _imageFactory.getIcon(file, false);
		if (icon.getIconWidth() > width) {
			icon = new ImageIcon(
				icon.getImage().getScaledInstance(
					width, -1, 
					Image.SCALE_DEFAULT));
			
			if (icon.getIconHeight() > height) {
				icon = new ImageIcon(
					icon.getImage().getScaledInstance(-1, 
						height, 
						Image.SCALE_DEFAULT));
			}
		}
		return icon;
	}
	
}
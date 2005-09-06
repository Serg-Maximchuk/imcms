package imcode.server.document.textdocument;

import com.imcode.util.ImageSize;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.Serializable;

public class ImageDomainObject implements Serializable {
    private ImageSource source;

    private String name = "";
    private int width;
    private int height;
    private int border;
    private String align = "";
    private String alternateText = "";
    private String lowResolutionUrl = "";
    private int verticalSpace;
    private int horizontalSpace;
    private String target = "";
    private String linkUrl = "";

    public String getName() {
        return name;
    }

    public ImageSize getDisplayImageSize() {
        ImageSize realImageSize = getRealImageSize( );

        int wantedWidth = getWidth( );
        int wantedHeight = getHeight( );
        if ( 0 == wantedWidth && 0 != wantedHeight && 0 != realImageSize.getHeight( ) ) {
            wantedWidth = (int)( realImageSize.getWidth( ) * ( (double)wantedHeight / realImageSize.getHeight( ) ) );
        } else if ( 0 == wantedHeight && 0 != wantedWidth && 0 != realImageSize.getWidth( ) ) {
            wantedHeight = (int)( realImageSize.getHeight( ) * ( (double)wantedWidth / realImageSize.getWidth( ) ) );
        } else if ( 0 == wantedWidth && 0 == wantedHeight ) {
            wantedWidth = realImageSize.getWidth( );
            wantedHeight = realImageSize.getHeight( );
        }
        return new ImageSize( wantedWidth, wantedHeight );
    }

    public ImageSize getRealImageSize() {
        ImageSize imageSize = new ImageSize( 0, 0 );
        if ( !isEmpty( ) ) {
            try {
                imageSize = source.getImageSize( );
            } catch ( IOException ignored ) {}
        }
        return imageSize;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getBorder() {
        return border;
    }

    public String getAlign() {
        return align;
    }

    public String getAlternateText() {
        return alternateText;
    }

    public String getLowResolutionUrl() {
        return lowResolutionUrl;
    }

    public int getVerticalSpace() {
        return verticalSpace;
    }

    public int getHorizontalSpace() {
        return horizontalSpace;
    }

    public String getTarget() {
        return target;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setName(String image_name) {
        this.name = image_name;
    }

    public void setWidth(int image_width) {
        this.width = image_width;
    }

    public void setHeight(int image_height) {
        this.height = image_height;
    }

    public void setBorder(int image_border) {
        this.border = image_border;
    }

    public void setAlign(String image_align) {
        this.align = image_align;
    }

    public void setAlternateText(String alt_text) {
        this.alternateText = alt_text;
    }

    public void setLowResolutionUrl(String low_scr) {
        this.lowResolutionUrl = low_scr;
    }

    public void setVerticalSpace(int v_space) {
        this.verticalSpace = v_space;
    }

    public void setHorizontalSpace(int h_space) {
        this.horizontalSpace = h_space;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setLinkUrl(String image_ref_link) {
        this.linkUrl = image_ref_link;
    }

    public void setSourceAndClearSize(ImageSource source) {
        setSource( source );
        setWidth( 0 );
        setHeight( 0 );
    }

    public void setSource(ImageSource source) {
        this.source = source;
    }

    public boolean isEmpty() {
        return null == source || source.isEmpty( );
    }

    public String getUrlPath(String contextPath) {
        String urlPathRelativeToContextPath = getUrlPathRelativeToContextPath( );
        if ( StringUtils.isBlank( urlPathRelativeToContextPath ) ) {
            return "";
        }
        return contextPath + urlPathRelativeToContextPath;
    }

    public String getUrlPathRelativeToContextPath() {
        if ( isEmpty( ) ) {
            return "";
        }
        return source.getUrlPathRelativeToContextPath( );
    }

    public long getSize() {
        if ( isEmpty( ) ) {
            return 0;
        }
        try {
            return source.getInputStreamSource( ).getSize( );
        } catch ( IOException e ) {
            return 0;
        }
    }

    public ImageSource getSource() {
        if ( isEmpty( ) ) {
            return new NullImageSource( );
        }
        return source;
    }
}

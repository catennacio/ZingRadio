package com.friends.zingradio.entity.json;

import java.util.ArrayList;
import android.os.Parcel;
import android.os.Parcelable;

public class Song implements Parcelable
{
    private String AlbumId;
    private String Artist;
    private String ArtistAvatar;
    private String Composer;
    private String CreatedDate;
    private ArrayList<GenreDetailContainer> GenreDetail;// = new ArrayList<GenreDetailContainer>();;
    private String Hit;
    private String ID;
    private String Link;
    private String LinkDownload128;
    private String LinkDownload320;
    private String LinkHtml5;
    private String LinkPlay128;
    private String LinkPlay24;
    private String LinkPlay320;
    private String LinkPlayEmbed;
    private String Lyrics;
    private String MobilePath;
    private String Official;
    private String OwnerAcc;
    private String Rington;
    private String Title;
    private String TotalListen;

    public String getAlbumId()
    {
        return AlbumId;
    }

    public String getArtist()
    {
        return Artist;
    }

    public String getArtistAvatar()
    {
        return ArtistAvatar;
    }

    public String getComposer()
    {
        return Composer;
    }
    
    public String getCreatedDate()
    {
        return CreatedDate;
    }

    
    public ArrayList<GenreDetailContainer> getGenreDetail()
    {
        return GenreDetail;
    }
    
    public String getHit()
    {
        return Hit;
    }

    public String getID()
    {
        return ID;
    }

    public String getLink()
    {
        return Link;
    }

    public String getLinkDownload128()
    {
        return LinkDownload128;
    }

    public String getLinkDownload320()
    {
        return LinkDownload320;
    }

    public String getLinkHtml5()
    {
        return LinkHtml5;
    }

    public String getLinkPlay128()
    {
        return LinkPlay128;
    }

    public String getLinkPlay24()
    {
        return LinkPlay24;
    }

    public String getLinkPlay320()
    {
        return LinkPlay320;
    }

    public String getLinkPlayEmbed()
    {
        return LinkPlayEmbed;
    }

    public String getLyrics()
    {
        return Lyrics;
    }

    public String getMobilePath()
    {
        return MobilePath;
    }

    public String getOfficial()
    {
        return Official;
    }

    public String getOwnerAcc()
    {
        return OwnerAcc;
    }

    public String getRington()
    {
        return Rington;
    }

    public String getTitle()
    {
        return Title;
    }

    public String getTotalListen()
    {
        return TotalListen;
    }

    
    public Song(){}
    
    public Song(Parcel in)
    {
        readFromParcel(in);
    }
    
    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {        
        dest.writeString(AlbumId);
        dest.writeString(Artist);
        dest.writeString(ArtistAvatar);
        dest.writeString(CreatedDate);
        dest.writeString(Hit);
        dest.writeString(ID);
        dest.writeString(Link);
        dest.writeString(LinkDownload128);
        dest.writeString(LinkDownload320);
        dest.writeString(LinkHtml5);
        dest.writeString(LinkPlay128);
        dest.writeString(LinkPlay24);
        dest.writeString(LinkPlay320);
        dest.writeString(LinkPlayEmbed);
        dest.writeString(Lyrics);
        dest.writeString(MobilePath);
        dest.writeString(Official);
        dest.writeString(OwnerAcc);
        dest.writeString(Rington);
        dest.writeString(Title);
        dest.writeString(TotalListen);
    }
    
    private void readFromParcel(Parcel in)
    {
        AlbumId = in.readString();
        Artist = in.readString();
        ArtistAvatar = in.readString();
        CreatedDate = in.readString();
        Hit = in.readString();
        ID = in.readString();
        Link = in.readString();
        LinkDownload128 = in.readString();
        LinkDownload320 = in.readString();
        LinkHtml5 = in.readString();
        LinkPlay128 = in.readString();
        LinkPlay24 = in.readString();
        LinkPlay320 = in.readString();
        LinkPlayEmbed = in.readString();
        Lyrics = in.readString();
        MobilePath = in.readString();
        Official = in.readString();
        OwnerAcc = in.readString();
        Rington = in.readString();
        Title = in.readString();
        TotalListen = in.readString();
    }
    
    public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>()
    {
        public Song createFromParcel(Parcel in)
        {
            return new Song(in);
        }

        public Song[] newArray(int size)
        {
            return new Song[size];
        }
    };
}


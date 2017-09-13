package com.friends.zingradio.entity.json;

public class SearchResult
{
    ArtistResult artist = new ArtistResult();
    AlbumResult album = new AlbumResult();
    SongResult song = new SongResult();
    
    public ArtistResult getArtistResult()
    {
        return artist;
    }
    
    public SongResult getSongResult()
    {
        return song;
    }

    public AlbumResult getAlbumResult()
    {
        return album;
    }
    
    public void free()
    {
        artist.free();
        album.free();
        song.free();
    }
}

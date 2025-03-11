import pytubefix
STORAGE_PATH = "storage/emulated/0/"

def downloadVideo(videoId):
  try:
    def on_progress(stream, chunk, bytes_remaining):
      progress = (1 - bytes_remaining / stream.filesize) * 100
      #print(f"Download {progress:.1f}%",flush=True)

    basePath = STORAGE_PATH+f"auditing/"
    video_url = f'https://www.youtube.com/watch?v={videoId}'
    #stream = audioStream(video_url, on_progress)
    #print(stream.filesize_kb, "kb" )
    yt = pytubefix.YouTube(video_url, on_progress_callback=on_progress)
    stream = yt.streams.get_audio_only()
    stream.download(basePath, videoId)
    return "downloaded on\n"+ basePath
    #print("\x1b[43mE\x1b[0m")
  except Exception as e:
    return f"Exception on python script: {e}"
def search(title):
  try:
    items = []
    for item in pytubefix.Search(title).all:
      if isinstance(item, pytubefix.YouTube):
        items.append("video: "+item.video_id)
      elif isinstance(item, pytubefix.Playlist):
          items.append("playlist: "+item.playlist_id)
      elif isinstance(item, pytubefix.Channel):
          items.append("channel: "+item.channel_id)
    return "\n".join(items)
  except Exception as e:
   return f"Exception on python script: {e}"


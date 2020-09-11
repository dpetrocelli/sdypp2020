FROM bitnami/minideb:latest
WORKDIR /usr/src/app
RUN apt update -y
RUN apt install ffmpeg -y
RUN apt install wget -y
RUN wget -O /tmp/video.mp4 "https://file-examples-com.github.io/uploads/2017/04/file_example_MP4_1920_18MG.mp4"
RUN echo "ffmpeg -y -i /tmp/video.mp4 -s 320x180 -aspect 16:9 -c:v libx264 -g 50 -b:v 220k -profile:v baseline -level 3.0 -r 15 -preset ultrafast -threads 0 -c:a aac -strict experimental -b:a 64k -ar 44100 -ac 2 /tmp/perfil_bajo.mp4" > script.sh
RUN echo "ffmpeg -y -i /tmp/video.mp4 -s 1920x1080 -aspect 16:9 -c:v libx264 -g 50 -b:v 22220k -profile:v high -level 5.0 -r 60 -preset slower -threads 0 -c:a aac -strict experimental -b:a 512k -ar 48000 -ac 6 /tmp/perfil_alto.mp4 & " >> script.sh
RUN echo "ffmpeg -y -i /tmp/video.mp4 -s 1920x1080 -aspect 16:9 -c:v libx264 -g 50 -b:v 22220k -profile:v high -level 5.0 -r 60 -preset slower -threads 0 -c:a aac -strict experimental -b:a 512k -ar 48000 -ac 6 /tmp/perfil_alto_v2.mp4" >> script.sh
RUN echo "sleep 30" >> script.sh
CMD ["bash", "/usr/src/app/script.sh"]
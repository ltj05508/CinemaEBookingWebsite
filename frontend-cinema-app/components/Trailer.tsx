"use client";
export default function Trailer({ url }: { url?: string }) {
  if (!url) return null;

  // naive YouTube -> embed transform
  const embed = url
    .replace("watch?v=", "embed/")
    .replace("youtu.be/", "www.youtube.com/embed/");

  return (
    <div className="aspect-video w-full overflow-hidden rounded-xl border">
      <iframe
        className="w-full h-full"
        src={embed}
        title="Trailer"
        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
        allowFullScreen
      />
    </div>
  );
}

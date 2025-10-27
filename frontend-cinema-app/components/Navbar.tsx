import Link from "next/link";

export default function Navbar() {
  return (
    <header className="sticky top-0 z-50 bg-ugared/80 backdrop-blur border-b">
      <nav className="mx-auto max-w-6xl px-4 h-14 flex items-center justify-between">
        <Link href="/" className="font-semibold text-lg">
          🎬 CineBook
        </Link>

        <div className="flex items-center gap-5 text-sm">
          <Link href="/movies" className="hover:underline">
            Movies
          </Link>
          <Link href="/about" className="hover:underline">
            About
          </Link>
          <Link href="/login" className="hover:underline">
            Login
          </Link>
        </div>
      </nav>
    </header>
  );
}

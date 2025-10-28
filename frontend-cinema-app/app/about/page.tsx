export default function AboutPage() {
    return (
      <main className="mx-auto max-w-4xl px-6 py-10 space-y-6">
        <h1 className="text-3xl font-semibold text-center">About CineBook</h1>
  
        <p className="text-base leading-relaxed text-gray-200">
          <strong>CineBook</strong> is a modern cinema booking web app designed to make finding and 
          reserving your favorite movies fast, simple, and enjoyable. 
          Our platform allows users to browse current and upcoming films, 
          view detailed information and trailers, and quickly secure seats for showtimes that fit their schedule.
        </p>
  
        <p className="text-base leading-relaxed text-gray-200">
          Built using <strong>Next.js</strong>, <strong>TypeScript</strong>, and <strong>Tailwind CSS</strong>, 
          CineBook offers a responsive, accessible, and smooth experience on any device. 
          Our goal is to combine beautiful design with performance — so you spend less time waiting and more time watching.
        </p>
  
        <section className="border-t border-white/10 pt-4">
          <h2 className="text-xl font-semibold mb-2">Our Mission</h2>
          <p className="text-base leading-relaxed text-gray-200">
            We’re passionate about bringing people together through the shared joy of movies. 
            CineBook was created to simplify the movie-going experience — from discovering films 
            to buying tickets — in one seamless interface.
          </p>
        </section>
  
        <section className="border-t border-white/10 pt-4">
          <h2 className="text-xl font-semibold mb-2">Contact</h2>
          <p className="text-base leading-relaxed text-gray-200">
            Have feedback or questions? Reach out at{" "}
            <a
              href="mailto:cinebook@example.com"
              className="text-ugared hover:underline"
            >
              cinebook@example.com
            </a>
          </p>
        </section>
      </main>
    );
  }
  
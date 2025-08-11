class LibraryController < ApplicationController
  def manage
    # Create a book
    book = Book.create(title: "The Ruby Way", author: "Hal Fulton", published_year: 2001)

    # Update the book
    book.update(published_year: 2024)

    # Find by title
    found_book = Book.find_by(title: "The Ruby Way")

    # Create an Ebook
    ebook = Ebook.create(title: "Eloquent Ruby", author: "Russ Olsen", published_year: 2011)

    # Example usage of Ebook method
    ebook_message = ebook.download

    render plain: "Book: #{found_book.title}, Year: #{found_book.published_year}\nEbook: #{ebook_message}"
  end
end

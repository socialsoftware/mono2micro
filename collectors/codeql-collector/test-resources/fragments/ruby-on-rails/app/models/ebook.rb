class Ebook < Book
  def download
    "Downloading #{title} by #{author}"
  end
end

module UsersHelper

  # Returns the Gravatar for the given user.
  def gravatar_for(user)
    gravatar_id = Digest::MD5::hexdigest(user.email.downcase)
    gravatar_url = "https://secure.gravatar.com/avatar/#{gravatar_id}?d=http%3A%2F%2Fwww.memes.at%2Ffaces%2Fmeh_cat.jpg"
    image_tag(gravatar_url, alt: user.name, class: "gravatar")
  end
end

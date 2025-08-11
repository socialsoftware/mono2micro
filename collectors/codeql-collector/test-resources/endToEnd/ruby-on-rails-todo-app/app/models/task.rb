class Task < ApplicationRecord
  belongs_to :user
  validates :taskText, presence: true,
                       length: { minimum: 1 }
end

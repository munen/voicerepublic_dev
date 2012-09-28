class Notification::MissedCall < Notification::Base 
  
  belongs_to :klu
  belongs_to :other, :class_name => 'User'  # other may be nil in case of anonymous call?
  
  validates :klu_id, :presence => true
  validates :url, :presence => true
  
  after_create :generate_push_notification
  
  private
  
  
  def generate_push_notification
    begin
      PrivatePub.publish_to("/notifications/#{user.id}", "alert('you missed a call!');")
    rescue Exception => e
      self.logger.error("Notification::MissedCall#generate_push_notification - error: #{e.inspect}")
    end  
  end
  
  
end
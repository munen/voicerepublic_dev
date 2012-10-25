class VideoSession::Registered < VideoSession::Base
  
  has_one :host_participant, :autosave => true, :class_name => 'Participant::Registered', :foreign_key => 'video_session_id', :dependent => :destroy
  has_one :guest_participant, :autosave => true, :class_name => 'Participant::Registered', :foreign_key => 'video_session_id', :dependent => :destroy
  
  before_create :prepare_one_on_one_video_session
  after_create :create_incoming_call_notification
  
  before_update :prepare_room_for_video_session
  after_update :create_call_accepted_notification
  
  before_destroy :create_call_canceled_notification
  
  validates_associated :host_participant, :guest_participant


 private
 
 def prepare_one_on_one_video_session  
    
   check_sezzion_create_prerequisites()
   
   #create guest (calling) participant for video_session
   self.guest_participant = Participant::Registered.new(:user_id => self.calling_user_id, :video_session_role => 'guest')
   
   #create host participant for video_session 
   self.host_participant = Participant::Registered.new(:user_id => @klu.user_id, :video_session_role => 'host')
  
  end
  
  def create_incoming_call_notification
    Notification::IncomingCall.create(:user_id => @klu.user_id, :other_id => self.calling_user_id, :video_session_id => self.id)  
  end
  
  def prepare_room_for_video_session
    #2, "#{t('sezzions_controller.update.chat_system_welcome')}", klu_show_url(:id => offer.id), request.host
    
    #raise KluuUException::RoomCreationFailed create_room_creation_failed_notification
      
  end
  
  def create_video_session_links_for_participants
    #raise KluuUException::LinkCreationFailed create_room_creation_failed_notification
  end  
  
  def create_call_accepted_notification  
  end
  
  def create_room_creation_failed_notification
  end
  
  def create_call_canceled_notification 
    if self.canceling_participant_id.to_i == self.guest_participant.id.to_i
      self.notifications.destroy_all
      Notification::MissedCall.create(:user_id => self.host_participant.user_id, :other_id => self.guest_participant.user_id, :video_session_id => self.id, :url => Rails.application.routes.url_helpers.user_path(:id => self.guest_participant.user_id))
    else  
      self.notifications.destroy_all
      Notification::CallRejected.create(:user_id => self.guest_participant.user_id, :other_id => self.host_participant.user_id, :video_session_id => self.id)
    end
  end
  
  def check_sezzion_create_prerequisites
    @klu = Klu.find(self.klu_id)
    @calling_user = User.find(self.calling_user_id)
    #is the klu unpublished or not existing?
    raise KluuuExceptions::KluUnavailableError.new(I18n.t('video_sessions_controller.create.failed_1'), 'shared/alert_flash') if (!@klu.published?)
    #is the user trying to call his own klu?
    raise KluuuExceptions::SameUserError.new(I18n.t('video_sessions_controller.create.failed_2'), 'shared/alert_flash') if (@calling_user.id == @klu.user_id)
    #is the klus user not available?
    raise KluuuExceptions::UserUnavailableError.new(I18n.t('video_sessions_controller.create.failed_3'), Rails.application.routes.url_helpers.new_message_path(:locale => I18n.locale, :receiver_id => @klu.user_id)) unless @klu.user.available?
    #if a registered user is calling a paid klu then make sure he has money
    raise KluuuExceptions::NoAccountError.new(I18n.t('video_sessions_controller.create.failed_4'), Rails.application.routes.url_helpers.new_user_balance_account_path(:locale => I18n.locale, :user_id => @calling_user.id)) if (@klu.charge_type != 'free') && (@calling_user.balance_account.nil?)
    #make sure the caller has at least credit for one paid minute
    raise KluuuExceptions::NoFundsError.new(I18n.t('video_sessions_controller.create.failed_6'), Rails.application.routes.url_helpers.edit_user_balance_account_path(:locale => I18n.locale, :user_id => @calling_user.id)) if ((@klu.charge_type != 'free') && (!@calling_user.balance_account.check_balance(@klu.charge, @klu.charge_type, 1)))
 
  end
end
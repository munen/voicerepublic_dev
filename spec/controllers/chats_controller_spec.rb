require 'spec_helper'

# This spec was generated by rspec-rails when you ran the scaffold generator.
# It demonstrates how one might use RSpec to specify the controller code that
# was generated by Rails when you ran the scaffold generator.
#
# It assumes that the implementation code is generated by the rails scaffold
# generator.  If you are using any extension libraries to generate different
# controller code, this generated spec may or may not pass.
#
# It only uses APIs available in rails and/or rspec-rails.  There are a number
# of tools you can use to make these specs even more expressive, but we're
# sticking to rails and rspec-rails APIs to keep things simple and stable.
#
# Compared to earlier versions of this generator, there is very limited use of
# stubs and message expectations in this spec.  Stubs are only used when there
# is no simpler way to get a handle on the object needed for the example.
# Message expectations are only used when there is no simpler way to specify
# that an instance is receiving a specific message.

describe ChatsController do
  before do 
    @user = FactoryGirl.create(:user)
    @recipient = FactoryGirl.create(:user)
    request.env['warden'].stub :authenticate! => @user
    controller.stub :current_user => @user
  end
  # This should return the minimal set of attributes required to create a valid
  # Chat. As you add validations to Chat, be sure to
  # update the return value of this method accordingly.
  def valid_attributes
    {}
  end

  # This should return the minimal set of values that should be in the session
  # in order to pass any filters (e.g. authentication) defined in
  # ChatsController. Be sure to keep this updated too.
  def valid_session
    {}
  end


  describe "GET new" do
    it "assigns a new chat as @chat - prerequisite is a running faye instance - otherwise an error occurs" do
      get :new, {:one => @recipient, :two => @user}, valid_session
      assigns(:chat).should be_a(Chat)
    end
  end


  describe "POST create" do
  #describe "with valid params" do
  #  it "creates a new Chat" do
  #    expect {
  #      post :create, {:chat => valid_attributes}, valid_session
  #    }.to change(Chat, :count).by(1)
  #  end
  #
  #  it "assigns a newly created chat as @chat" do
  #    post :create, {:chat => valid_attributes}, valid_session
  #    assigns(:chat).should be_a(Chat)
  #    assigns(:chat).should be_persisted
  #  end
  #
  #  it "redirects to the created chat" do
  #    post :create, {:chat => valid_attributes}, valid_session
  #    response.should redirect_to(Chat.last)
  #  end
  #end
  #
  #describe "with invalid params" do
  #  it "assigns a newly created but unsaved chat as @chat" do
  #    # Trigger the behavior that occurs when invalid params are submitted
  #    Chat.any_instance.stub(:save).and_return(false)
  #    post :create, {:chat => {}}, valid_session
  #    assigns(:chat).should be_a_new(Chat)
  #  end
  #
  #  it "re-renders the 'new' template" do
  #    # Trigger the behavior that occurs when invalid params are submitted
  #    Chat.any_instance.stub(:save).and_return(false)
  #    post :create, {:chat => {}}, valid_session
  #    response.should render_template("new")
  #  end
  #end
  end

  

  describe "DELETE destroy" do
  # it "destroys the requested chat" do
  #   chat = Chat.create! valid_attributes
  #   expect {
  #     delete :destroy, {:id => chat.to_param}, valid_session
  #   }.to change(Chat, :count).by(-1)
  # end
  #
  # it "redirects to the chats list" do
  #   chat = Chat.create! valid_attributes
  #   delete :destroy, {:id => chat.to_param}, valid_session
  #   response.should redirect_to(chats_url)
  # end
  end

end

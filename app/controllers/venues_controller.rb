class VenuesController < ApplicationController

  before_filter :store_location
  #before_filter :remember_location, :only => [:join_venue]
  before_filter :authenticate_user!, :except => [:index, :show, :tags]
  before_filter :set_venue, only: [:show, :edit, :update, :destroy]

  # GET /venues
  # GET /venues.json
  def index
    @venues = Venue.order('updated_at DESC')

    respond_to do |format|
      format.html # index.html.erb
      format.json { render json: @venues }
    end
  end

  # GET /venues/1
  # GET /venues/1.json
  def show
    @upcoming_talks = @venue.talks.prelive
    @next_talk      = @upcoming_talks.shift
    @archived_talks = @venue.talks.archived

    @participation =
      @venue.participations.find_by(user_id: current_user.id)

    @show_join = @participation.nil? &&
      current_user != @venue.user

    respond_to do |format|
      format.html # show.html.erb
      format.json { render json: @venue }
    end
  end

  # GET /venues/new
  # GET /venues/new.json
  def new
    @venue = Venue.new

    respond_to do |format|
      format.html # new.html.erb
      format.json { render json: @venue }
    end
  end

  # GET /venues/1/edit
  def edit
    if params[:renew]
      @renew = true
    end
    authorize! :edit, @venue
    respond_to do |format|
      format.html {}
      format.js {}
    end
  end

  # POST /venues
  # POST /venues.json
  def create
    @venue = Venue.new(params[:venue])
    @venue.user = current_user

    authorize! :create, @venue

    respond_to do |format|
      if @venue.save
        format.html do
          logger.debug("Venues#cretae  - redirecting to venue")
          redirect_to @venue, notice: 'Venue was successfully created.'
         end
        format.json { render json: @venue, status: :created, location: @venue }
      else
        logger.error("Venues#create - ERROR: #{@venue.errors.inspect}")
        format.html { render action: "new" }
        format.json { render json: @venue.errors, status: :unprocessable_entity }
      end
    end
  end

  # PUT /venues/1
  # PUT /venues/1.json
  def update
    authorize! :update, @venue

    respond_to do |format|
      if @venue.update_attributes(params[:venue])
        format.html { redirect_to @venue, notice: 'Venue was successfully updated.' }
        format.json { head :no_content }
      else
        format.html { render action: "edit" }
        format.json { render json: @venue.errors, status: :unprocessable_entity }
      end
    end
  end

  def end_event
    @venue = Venue.find(params[:id])
    @event = @venue.events.find(params[:event_id])
    @event.end_at = Time.now.in_time_zone

    if @event.save
      render nothing: true
    else
      render text: @event.errors.full_messages.join(', '), status: :unprocessable_entity
    end
  end

  def remove_recording
    @venue = Venue.find(params[:id])
    @event = @venue.events.find(params[:event_id])
    File.delete("#{Venue::RECORDINGS_PATH}/#{@event.recording}")
    @event.update_column(:recording, nil)
    redirect_to @venue, notice: 'Recording was successfully removed.'
  end

  # # POST /venues/1/join_venue/5
  # #
  # def join_venue
  #
  #   logger.debug("Venues#join_venue - at start of function ####################### ")
  #
  #   @venue = Venue.find(params[:venue_id])
  #
  #   if current_user.no_kluuus.empty?
  #     klu = current_user.no_kluuus.create(:title => current_user.name, :published => true, :tag_list => "kluser", :category => Category.first)
  #   else
  #     klu = current_user.no_kluuus.first
  #   end
  #
  #   #klu = Klu.find(params[:klu_id])
  #
  #   venue_klu = VenueKlu.new(:venue => @venue, :klu => klu )
  #
  #   authorize! :create, venue_klu
  #
  #   respond_to do |format|
  #     if venue_klu.save
  #       format.html { redirect_to @venue, notice: "Successfully joined venue" }
  #       format.json { head :no_content }
  #     else
  #       format.html { redirect_to @venue, alert: "Wasn't not able to join venue - perhaps already subscribed?" }
  #       format.json { head :no_content }
  #     end
  #   end
  # end
  #
  # def unjoin_venue
  #   @venue = Venue.find(params[:venue_id])
  #   @venue.venue_klus.collect { |vk| vk.destroy if vk.klu.user == current_user }
  #
  #   respond_to do |format|
  #     format.html { redirect_to @venue, notice: "Successfully unjoined venue" }
  #     #format.js {}
  #   end
  # end

  def new_join
    @venue = Venue.find(params[:venue_id])
    respond_to do |format|
      format.html
      format.js
    end
  end

  # DELETE /venues/1
  # DELETE /venues/1.json
  def destroy
    authorize! :destroy, @venue

    @venue.destroy

    respond_to do |format|
      format.html { redirect_to user_path(current_user) }
      format.json { head :no_content }
    end
  end

  def tags
    scope = ActsAsTaggableOn::Tag.where(["name ILIKE ?", "%#{params[:q]}%"])
    tags = scope.paginate(:page => params[:page], :per_page => params[:limit] || 10)
    render json: { tags: tags, total: scope.count }
  end

  private

  def remember_location
    logger.debug("Venues#remember_location - storing location: " +
                 "#{venue_url(:id => params[:venue_id])}")
    session[:venue_path] = venue_url(:id => params[:venue_id])
  end

  def set_venue
    @venue = Venue.find(params[:id])
  end

end

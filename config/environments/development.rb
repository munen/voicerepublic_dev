Rails.application.configure do
  # Settings specified here will take precedence over those in config/application.rb

  config.eager_load = false

  # In the development environment your application's code is reloaded on
  # every request. This slows down response time but is perfect for development
  # since you don't have to restart the web server when you make code changes.
  config.cache_classes = false

  # Do not eager load code on boot.
  config.eager_load = false

  # Show full error reports and disable caching
  config.consider_all_requests_local       = true
  config.action_controller.perform_caching = false

  # Don't care if the mailer can't send
  config.action_mailer.raise_delivery_errors = false
  #config.action_mailer.delivery_method = :file
  config.action_mailer.delivery_method = :letter_opener_web

  ActionMailer::Base.file_settings = { :location => Rails.root.join('tmp/mail') }
  #config.action_mailer.smtp_settings = {
  #                                        :address              => "smtp.gmail.com",
  #                                        :port                 => 587,
  #                                        :domain               => 'baci.lindsaar.net',
  #                                        :user_name            => '<username>',
  #                                        :password             => '<password>',
  #                                        :authentication       => 'plain',
  #                                        :enable_starttls_auto => true  }
  #

  # Print deprecation notices to the Rails logger
  config.active_support.deprecation = :log

  # Raise an error on page load if there are pending migrations
  config.active_record.migration_error = :page_load

  # Expands the lines which load the assets
  config.assets.debug = true

  # Asset digests allow you to set far-future HTTP expiration dates on all assets,
  # yet still be able to expire them through the digest params.
  config.assets.digest = true

  # Adds additional error checking when serving assets at runtime.
  # Checks for improperly declared sprockets dependencies.
  # Raises helpful error messages.
  config.assets.raise_runtime_errors = true

  # Raises error for missing translations
  # config.action_view.raise_on_missing_translations = true

  # fixes generating return_url for paypal in dev env
  config.after_initialize do
    Rails.application.routes.default_url_options[:host] =
      Settings.dev_host_and_port || 'localhost:3000'
  end

end

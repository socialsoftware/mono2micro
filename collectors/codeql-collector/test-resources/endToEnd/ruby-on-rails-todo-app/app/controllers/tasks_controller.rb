class TasksController < ApplicationController
  def create
    @user = User.find(params[:user_id])
    @task = @user.tasks.create(task_params)
    redirect_to user_path(@user)
  end

  def destroy
    @user = User.find(params[:user_id])
    @task = @user.tasks.find(params[:id])
    @task.destroy
    redirect_to user_path(@user)
  end

  private
    def task_params
      params.require(:task).permit(:taskText)
    end
end

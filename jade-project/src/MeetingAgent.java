package jadeproject;

import jade.core.Agent;

import java.util.HashSet;
import java.util.Random;
import java.util.ArrayList;
import jade.lang.acl.UnreadableException;
import jade.lang.acl.ACLMessage;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.MessageTemplate;

public class MeetingAgent extends Agent {
    private Random random = new Random();
	private final long TIMEOUT = 1000;
	
    private MeetingAgentGui gui;//The graphic User interface
	private final String agentType = "meeting-agent";//Type of agent that we are
	private boolean scheduling = false;//Flag that indicate if we are scheduling a meeting
	
	private WeekCalendar calendar = new WeekCalendar();//Calendar random for the Agent
	ArrayList<int[]> availableSlots= calendar.findSlots(); //Free Slots in the calendar created
	

	
	@Override
	protected void setup() {
		gui = new MeetingAgentGui(this);
		gui.display();//We display in the screen the gui that we programmed
		System.out.println(getAID().getLocalName() + ": Ready for the action!");//Message to print that the Agent is ready in the cmd

		DFAgentDescription dfAgentDescription = new DFAgentDescription();//We generate the description of the Agent
		dfAgentDescription.setName(getAID());//Set the name to a AgentID

		ServiceDescription serviceDescription = new ServiceDescription();//We generate the description of the service of the Agent
		serviceDescription.setType(agentType);//Set the type to meeting-agent
		serviceDescription.setName(getAID().getLocalName());//The name to the local name of the Agent ID

		dfAgentDescription.addServices(serviceDescription);//We add to the Agent Description the description of the service that the Agent do

		try {
			DFService.register(this, dfAgentDescription);//We register the Agent with the description of itsself
		} catch (FIPAException ex) {
			ex.printStackTrace();
		}
		
        addBehaviour(new ReceiveCancelation());//We add the three behaviour of the Agent: recieve cancelation, invitation and confirmation
		addBehaviour(new ReceiveInvitation());
		addBehaviour(new ReceiveConfirmation());
		
		
	}
	
	
	public void programMeeting() {//One shot behaviour
		System.out.println(getAID().getLocalName() + ": is going to set a Meeting!");
		scheduling = true;
		addBehaviour(new BookBehavior());
	}
	
	@Override
	protected void takeDown() {
		gui.dispose();
		try {
			DFService.deregister(this);
		} catch (FIPAException ex) {
			ex.printStackTrace();
		}

		System.out.println(getAID().getLocalName() + ": See you later alligator!");//Message of finish of a Agent taken down ;)
	}
	
	private class BookBehavior extends Behaviour {
		private int numberOfAttendees = 0;
		private int count_replies1 = 0;
		private int count_replies2 = 0;
		private AID[] attendees;
		private AID[] agents;
		private int[] slot;
		private int step = 0;
		private long begin, end;
		private HashSet<int[]> meetingTimes = new HashSet<int[]>();
		private MessageTemplate template;

		

		@Override
		public void action() {
			if (scheduling) {
				switch (step) {
					case 0:
						System.out.println(myAgent.getLocalName() + ": is looking for people to invite"); //Start message

						DFAgentDescription dfTemplate = new DFAgentDescription();
						ServiceDescription serviceDescription = new ServiceDescription();

						serviceDescription.setType(agentType);//we set to the type of agent.
						dfTemplate.addServices(serviceDescription);//we add the service to the template

						try {
							DFAgentDescription[] result = DFService.search(myAgent, dfTemplate); //We look for all the people to invite
							
							System.out.println(getAID().getLocalName() + ": We found this guys to be invited:");
							agents = new AID[result.length - 1];

							int aux = 0;
							for (int i = 0; i < result.length; ++i){//In this for we check that if the name its diferent, we add to the list of agents to invite, and we show in the screen that we found them
								if (!result[i].getName().equals(getAID().getName())) {
									agents[aux] = result[i].getName();
									System.out.println(myAgent.getLocalName() + ": found " + agents[aux++].getLocalName());
								}
							}
						} catch (FIPAException ex) {
							ex.printStackTrace();
						}

						System.out.println(myAgent.getLocalName() + ": is deciding the list of invited people for the meeting...");
						
						int maximum = agents.length;
						System.out.println("Maximun number of people to invite:"+maximum);
						if (maximum > 0) numberOfAttendees = random.nextInt(maximum) + 1;//We randomize the number of people invited to the meeting

						System.out.println(getAID().getLocalName() + ": has decided to invite " + numberOfAttendees + " peoples to the meeting!");

						if (numberOfAttendees != 0) {
							attendees = new AID[numberOfAttendees];
							System.out.println(getAID().getLocalName() + ": Invited people list:");
							
							for (int i = 0; i < numberOfAttendees; ++i) {
								if (!agents[i].getName().equals(getAID().getName())) {
								attendees[i] = agents[i];
								System.out.println(getAID().getLocalName() + ": Attendee" + (i + 1) + ": " + attendees[i].getLocalName());
								}
							}

							System.out.println(getAID().getLocalName() + ": Now is selecting a meeting time");
							slot = availableSlots.get(random.nextInt(availableSlots.size() - 1)); //We randomize from the list of avaliableSlots

							if (slot == null) {
								// No free time found in the calendar, go to the end
								System.out.println(getAID().getLocalName() + ": doesn´t have free time to held the meeting");
								step = 10;
							} else {
								int day = slot[0], hour = slot[1]; //In the method  we add one day and one hour
								System.out.println(getAID().getLocalName() + ": Proposed meeting date: " + WeekCalendar.getWeekDayName(day) + " at " + hour + "H00");
								meetingTimes.add(slot);//we add to the meeting times the slot that we found on our calendar
								step = 1;//Move to the next step
							}
						} else {
							step = 10; //If we dont have attendees it moves to the end
						}
						
						break;
					
					case 1:
						begin = System.currentTimeMillis();

						System.out.println(getAID().getLocalName() + ": begin to send invitations");
						ACLMessage message = new ACLMessage(ACLMessage.CFP);//generate the message that we are going to send
						
						int i=0;
						while(i<numberOfAttendees){
							message.addReceiver(attendees[i]); //We add the receivers of the message, all the invited people
							i++;
						}

						message.setContent(Integer.toString(slot[0]) + "," + Integer.toString(slot[1])); //We put in the content the day and hour
						message.setConversationId("schedule-meeting");
						message.setReplyWith("cfp" + System.currentTimeMillis());
						
						// we need now to block the slot to be offered for other meeting
						calendar.scheduleMeeting(slot[0], slot[1]);
						availableSlots = calendar.findSlots();

						myAgent.send(message);//We send the message to the people invited
						System.out.println(getAID().getLocalName() + ": Send the invitations, waiting for the response");

						template = MessageTemplate.and(MessageTemplate.MatchConversationId("schedule-meeting"),
							MessageTemplate.MatchInReplyTo(message.getReplyWith())); //We match the template with the Id that we put before and finding by the Replywith of the message.
						
						
						step = 2;
						count_replies1=0;
						break;
					case 2:
						ACLMessage reply = myAgent.receive(template);
						
						end = System.currentTimeMillis();
						double timeElapsed = end - begin; //We apply here what we learn in the labs putting a timeout to check the response of the agents.

						if (timeElapsed > TIMEOUT) {
							sendCancelation(Integer.toString(slot[0]) + "," + Integer.toString(slot[1]), null);
							step = 10;//If the time is more than the Timeout,  go to the end
						} else {
							if (reply != null) {//If we get a response lets see what they reply.
								if (reply.getPerformative() == ACLMessage.REJECT_PROPOSAL) {//They reject the proposal
									System.out.println(getAID().getLocalName() + ": " + reply.getSender().getLocalName() + " rejected the meeting!");

									AID rejectingAgent = reply.getSender();//we obtain the invited that reject the meeting

									int day_cancel = slot[0], hour_cancel = slot[1];
									calendar.cancelMeeting(day_cancel, hour_cancel);//We cancel the meeting that we offered the first time
									availableSlots = calendar.findSlots();//We look for new slots
									
									for (int[] proposedMeetingTime : meetingTimes) {
										availableSlots.remove(proposedMeetingTime);//we delete from the slots the proposed meeting
									}

									if (availableSlots.size() > 1) slot = availableSlots.get(random.nextInt(availableSlots.size() - 1)); //we randomize again the slot
									else if (availableSlots.size() == 1) slot = availableSlots.get(0); // only one slot is disponible
									else slot = null; // there aren´t slots
									
									if (slot == null) {
										System.out.println(getAID().getLocalName() + ": Doesn´t have free time for a meeting");
										step = 10;//we go to the end
									} else {
										int day = slot[0];int hour = slot[1];// we have one slot we set day and hour

										System.out.println(getAID().getLocalName() + ": Change the plans of the meeting, it will be the day: " + WeekCalendar.getWeekDayName(day) + " at " + hour + "H00");

										step = 1;//we go back to the step one to send the invitations
									}
									
									System.out.println(getAID().getLocalName() + ": is going to send again the invitations with the change of plans");
									
									sendCancelation(Integer.toString(day_cancel) + "," + Integer.toString(hour_cancel), rejectingAgent);//We send the cancelation message to the one that reject the invitation

								} else {//If we dont have trubles in the schedule just accept the meeting :)
									System.out.println(getAID().getLocalName() + ": " + reply.getSender().getLocalName() + " accepted the meeting :)");
									count_replies1++;
									if (count_replies1 == attendees.length) {
										step = 3;//When we have all the replies we go to the next step.
									}
								}
							} else {
								block(TIMEOUT);
							}
						}
						break;
					case 3: //Everyone can assist to the meeting, we send the confirmation meeting message
						System.out.println(getAID().getLocalName() + ": Is sending the message of confirmation of the meeting");

						begin = System.currentTimeMillis();

						ACLMessage confirmationMessage = new ACLMessage(ACLMessage.CONFIRM); //we create the message
						for (int k = 0; k < numberOfAttendees; ++k) {
							confirmationMessage.addReceiver(attendees[k]);//we add the recievers
						}

						confirmationMessage.setContent(Integer.toString(slot[0]) + "," + Integer.toString(slot[1])); //the content is the day and hour of meeting
						confirmationMessage.setConversationId("schedule-meeting");
						confirmationMessage.setReplyWith("confirm" + System.currentTimeMillis());
	
						myAgent.send(confirmationMessage);//We send the message

						System.out.println(getAID().getLocalName() + ": Had sent all the messages of confirmation to the people invited");

						template = MessageTemplate.and(MessageTemplate.MatchConversationId("schedule-meeting"),
							MessageTemplate.MatchInReplyTo(confirmationMessage.getReplyWith()));//We match the template with the Id that we put before and finding by the Replywith of the message.
						count_replies2=0;
						step = 4;

					case 4:
						// Receive the ACK of the invited people to the meeting
						ACLMessage ackMessage = myAgent.receive(template);
						
						end = System.currentTimeMillis();
						timeElapsed = end - begin;

						if (timeElapsed > TIMEOUT) {//We checked like before if the people reply that we have a meeting confirmed
							sendCancelation(Integer.toString(slot[0]) + "," + Integer.toString(slot[1]), null);
							step = 10;//If not we send message of cancelation and we move ot the end
						} else {
							if (ackMessage != null) {
								if (ackMessage.getPerformative() == ACLMessage.AGREE) {//If they agree
									AID agent = ackMessage.getSender();

									System.out.println(getAID().getLocalName() + ": received the ack message from " + agent.getLocalName());//we say that we received perfect the ACK

									count_replies2++;
									if (count_replies2 == attendees.length) {//If we have all the acks we move to the last step.
										step = 5;
									}
								}
							} else {
								block(TIMEOUT);
							}
						}
						break;
					case 5:
						// We finish to book the meeting we print the final meeeting
						int day = slot[0], hour = slot[1];

						System.out.println(getAID().getLocalName() + ": Finaly the Meeting will be the day " + WeekCalendar.getWeekDayName(day) + " at " + hour + "H00");
					default:
						System.out.println(getAID().getLocalName() + ": Error when scheduling the meeting!");//if we get an error we move to the end
						step = 10;
						break;

				}
			}
		}
		
		
		private void sendCancelation(String content, AID agentReject) {
			//This method will sent a cancelation message to the agents that reject the invitation
			
			System.out.println(getAID().getLocalName() + ": is sending cancelation messages to the agents that rejected the invitation...");
			ACLMessage cancelationMessage = new ACLMessage(ACLMessage.CANCEL);//we generate a cancel message
			
			int i=0;
			while (i<numberOfAttendees){
				if (agentReject != null) {
					if (!agentReject.equals(attendees[i])) cancelationMessage.addReceiver(attendees[i]);//we set the recievers
				}
				i++;
			}

			cancelationMessage.setContent(content);
			cancelationMessage.setConversationId("schedule-meeting");
			cancelationMessage.setReplyWith("cancel" + System.currentTimeMillis());

			myAgent.send(cancelationMessage);//We send the cancel messages

			System.out.println(getAID().getLocalName() + ":sent the cancelation messages!");
		}
		
		
		@Override
		public boolean done() {//It checks if the case become to the end if its good with 5 steps or something went wrong with the "flag" of step 10
			if (step == 10 || step == 5) {
				scheduling = false;
				return true;
			} else {
				return false;
			}
		}
		
		
		
	}
	
	private class ReceiveInvitation extends CyclicBehaviour  {
		@Override
		public void action() {
			MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage message = myAgent.receive(template);// We generate the message received for the agents with the template of CFP
			
			if (message != null) {
				AID invitingAgent = message.getSender(); //we get who send this message, the agent

				System.out.println(getAID().getLocalName() + ": received an invitation for a meeting from " + invitingAgent.getLocalName());
				
				String messageContent = message.getContent(); //We get the content of the message: the slots format day,hour
				String[] day_hour = messageContent.split(",");//We divide the string into [day,hour]

				int day, hour;
				day = Integer.parseInt(day_hour[0]);
				hour = Integer.parseInt(day_hour[1]);

				System.out.println(getAID().getLocalName() + ": Has an invitation for a meeting the day: " + WeekCalendar.getWeekDayName(day) + " at " +  hour + "H00. Checking availability...");

				if (calendar.isAvailable(day, hour)) {
					System.out.println(getAID().getLocalName() + ": is available for the meeting! Preparing message of aceptance");

					calendar.scheduleMeeting(day, hour);//we schedule a Meeting 

					ACLMessage acceptance = message.createReply(); //we create a message reply for aceptance the invitation
					acceptance.setContent(messageContent);//the content before day,hour
					acceptance.setPerformative(ACLMessage.INFORM); 

					myAgent.send(acceptance);//We send the message of acceptance
					System.out.println(getAID().getLocalName() + ": Aceptance message sent! Waiting for confirmation...");

				} else {
					System.out.println(getAID().getLocalName() + ": is not available for the meeting! Preparing Rejection message");

					ACLMessage rejection = message.createReply();//We create the message
					
					rejection.setContent(messageContent);// content day,hour
					rejection.setPerformative(ACLMessage.REJECT_PROPOSAL);

					myAgent.send(rejection);//send the reject

					System.out.println(getAID().getLocalName() + ": message of rejection send! Wainting for different proposals...");

				}
			} else {
				block();
			}
		}
	}

	private class ReceiveCancelation extends CyclicBehaviour {
		@Override
		public void action() {
			MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.CANCEL);//Cancel message as a template
			ACLMessage message = myAgent.receive(template);// generate the message

			if (message != null) {
				AID cancelingAgent = message.getSender(); //We get the agent who send the message of cancelation

				String messageContent = message.getContent(); // day,hour as a content, (as always :) )
				String[] day_hour = messageContent.split(","); //we divide the string between day and hour.

				int day, hour;
				day = Integer.parseInt(day_hour[0]);
				hour = Integer.parseInt(day_hour[1]);
				int[] meetingTime = {day, hour};

				System.out.println(getAID().getLocalName() + ": received a cancelation message from " + cancelingAgent.getLocalName()  + " for a meeting the day: " + WeekCalendar.getWeekDayName(day) + "at " + hour + "H00");

				calendar.cancelMeeting(day, hour);

				System.out.println(getAID().getLocalName() + ": meeting the day:" + WeekCalendar.getWeekDayName(day) + " at " + hour + "H00 has been cancelled");
			} else {
				block(TIMEOUT);
			}
		}
	}

	private class ReceiveConfirmation extends CyclicBehaviour {
		@Override
		public void action() {
			MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM); //template of confirmation
			ACLMessage message = myAgent.receive(template); //generate the message

			if (message != null) {
				ACLMessage ackMessage = message.createReply(); //generate ackMessage replying the confirmation
				String messageContent = message.getContent();	// content day,hour 
				String[] day_hour = messageContent.split(",");  //we split the content in [day,hour]

				int day, hour;
				day = Integer.parseInt(day_hour[0]);
				hour = Integer.parseInt(day_hour[1]);

				System.out.println(getAID().getLocalName() + ": received the confirmation for the meeting on " + WeekCalendar.getWeekDayName(day) + " at " +  hour + "H00. Sending Ack");

				ackMessage.setContent(messageContent); //We set the content to the day,hour
				ackMessage.setPerformative(ACLMessage.AGREE); //the performtive is afirmative
				
				myAgent.send(ackMessage);//we send the message

				System.out.println(getAID().getLocalName() + ": ack sent!");
			} else {
				block(TIMEOUT);
			}
		}
	}
}
	
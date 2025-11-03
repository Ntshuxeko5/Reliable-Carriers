/**
 * Enhanced AI-Powered Chat System
 * Includes typing indicators, quick responses, and better UX
 */

class EnhancedChatSystem {
    constructor(chatId, userEmail) {
        this.chatId = chatId;
        this.userEmail = userEmail;
        this.stompClient = null;
        this.isConnected = false;
        this.messages = [];
        this.isTyping = false;
        this.typingTimeout = null;
    }

    async init() {
        await this.loadChatHistory();
        this.connectWebSocket();
        this.setupUI();
        this.showWelcomeMessage();
    }

    connectWebSocket() {
        if (typeof SockJS === 'undefined' || typeof Stomp === 'undefined') {
            console.warn('WebSocket libraries not loaded. Using REST fallback.');
            return;
        }

        const socket = new SockJS('/ws');
        this.stompClient = Stomp.over(socket);

        this.stompClient.connect({}, (frame) => {
            console.log('Chat WebSocket connected:', frame);
            this.isConnected = true;
            this.subscribeToChat();
            this.updateConnectionStatus(true);
        }, (error) => {
            console.error('Chat WebSocket connection error:', error);
            this.isConnected = false;
            this.updateConnectionStatus(false);
        });
    }

    subscribeToChat() {
        if (!this.stompClient || !this.isConnected) return;

        this.stompClient.subscribe('/topic/chat/' + this.chatId, (message) => {
            const data = JSON.parse(message.body);
            this.handleMessage(data);
        });
    }

    async loadChatHistory() {
        try {
            const response = await fetch(`/api/chat/history/${this.chatId}`);
            const result = await response.json();

            if (result.success && result.messages) {
                this.messages = result.messages;
                this.displayMessages();
            }
        } catch (error) {
            console.error('Error loading chat history:', error);
        }
    }

    sendMessage(text) {
        if (!text || text.trim().length === 0) return;

        const message = {
            chatId: this.chatId,
            senderEmail: this.userEmail,
            text: text.trim(),
            timestamp: new Date().toISOString()
        };

        // Display user message immediately
        this.addMessage({
            ...message,
            type: 'MESSAGE',
            senderName: 'You'
        }, true);

        // Show typing indicator
        this.showTypingIndicator();

        // Try WebSocket first
        if (this.isConnected && this.stompClient) {
            this.stompClient.send('/app/send', {}, JSON.stringify(message));
        } else {
            // Fallback to REST API
            this.sendMessageRest(message);
        }

        // Clear input
        const messageInput = document.getElementById('chatMessageInput');
        if (messageInput) {
            messageInput.value = '';
        }
    }

    async sendMessageRest(message) {
        try {
            await fetch('/api/chat/send', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(message)
            });
        } catch (error) {
            console.error('Error sending message via REST:', error);
            this.showError('Failed to send message. Please try again.');
            this.hideTypingIndicator();
        }
    }

    handleMessage(messageData) {
        if (messageData.type === 'BOT_RESPONSE') {
            this.hideTypingIndicator();
        }

        this.addMessage(messageData, false);
    }

    addMessage(messageData, isUserMessage) {
        // Don't add duplicate messages
        if (this.messages.some(m => 
            m.text === messageData.text && 
            m.timestamp === messageData.timestamp
        )) {
            return;
        }

        this.messages.push(messageData);
        this.displayMessage(messageData, isUserMessage);
        this.scrollToBottom();
    }

    displayMessages() {
        const messagesContainer = document.getElementById('chatMessages');
        if (!messagesContainer) return;

        messagesContainer.innerHTML = '';
        this.messages.forEach(message => {
            this.displayMessage(message, message.senderEmail === this.userEmail);
        });
        this.scrollToBottom();
    }

    displayMessage(message, isUserMessage) {
        const messagesContainer = document.getElementById('chatMessages');
        if (!messagesContainer) return;

        // Remove empty state if exists
        const emptyState = messagesContainer.querySelector('.empty-state');
        if (emptyState) emptyState.remove();

        const isBot = message.type === 'BOT_RESPONSE' || 
                     message.senderEmail === 'support@reliablecarriers.co.za';

        const messageDiv = document.createElement('div');
        messageDiv.className = `flex ${isUserMessage ? 'justify-end' : 'justify-start'} mb-4`;
        messageDiv.setAttribute('data-message-id', Date.now());

        if (isBot) {
            messageDiv.innerHTML = this.createBotMessageHTML(message);
        } else {
            messageDiv.innerHTML = this.createUserMessageHTML(message);
        }

        messagesContainer.appendChild(messageDiv);

        // Add quick responses if available
        if (message.quickResponses && message.quickResponses.length > 0) {
            this.addQuickResponses(message.quickResponses, messageDiv);
        }

        // Add metadata if available (e.g., tracking info)
        if (message.metadata) {
            this.addMetadataDisplay(message.metadata, messageDiv);
        }

        // Handle agent handoff
        if (message.requiresHuman) {
            this.showAgentHandoffOption(messageDiv);
        }
    }

    createBotMessageHTML(message) {
        const senderName = message.senderName || 'Support Assistant';
        const text = this.formatMessage(message.text);
        const time = this.formatTime(message.timestamp);

        return `
            <div class="flex items-start space-x-2 max-w-xs lg:max-w-md">
                <div class="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center flex-shrink-0">
                    <i class="fas fa-robot text-blue-600 text-sm"></i>
                </div>
                <div class="bg-white border border-gray-200 rounded-lg px-4 py-2 shadow-sm">
                    <div class="text-xs font-semibold text-blue-600 mb-1">${this.escapeHtml(senderName)}</div>
                    <div class="text-sm text-gray-900 whitespace-pre-wrap">${text}</div>
                    <div class="text-xs text-gray-400 mt-1">${time}</div>
                </div>
            </div>
        `;
    }

    createUserMessageHTML(message) {
        const text = this.escapeHtml(message.text);
        const time = this.formatTime(message.timestamp);

        return `
            <div class="bg-blue-500 text-white rounded-lg px-4 py-2 max-w-xs lg:max-w-md shadow-sm">
                <div class="text-sm whitespace-pre-wrap">${text}</div>
                <div class="text-xs text-blue-100 mt-1">${time}</div>
            </div>
        `;
    }

    formatMessage(text) {
        // Format markdown-like syntax, links, etc.
        let formatted = this.escapeHtml(text);
        
        // Convert line breaks
        formatted = formatted.replace(/\n/g, '<br>');
        
        // Convert bullet points
        formatted = formatted.replace(/^•\s/gm, '<span class="inline-block w-2 h-2 bg-blue-500 rounded-full mr-2"></span>');
        
        // Convert links
        formatted = formatted.replace(/(https?:\/\/[^\s]+)/g, 
            '<a href="$1" target="_blank" class="text-blue-600 hover:underline">$1</a>');
        
        return formatted;
    }

    addQuickResponses(quickResponses, messageDiv) {
        const quickResponseDiv = document.createElement('div');
        quickResponseDiv.className = 'mt-2 ml-10 flex flex-wrap gap-2';
        
        quickResponses.forEach(response => {
            const button = document.createElement('button');
            button.className = 'px-3 py-1 text-xs bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-full border border-gray-300 transition-colors';
            button.textContent = response;
            button.addEventListener('click', () => {
                this.sendMessage(response);
            });
            quickResponseDiv.appendChild(button);
        });
        
        messageDiv.appendChild(quickResponseDiv);
    }

    addMetadataDisplay(metadata, messageDiv) {
        if (!metadata.trackingNumber) return;

        const metadataDiv = document.createElement('div');
        metadataDiv.className = 'mt-2 ml-10 bg-blue-50 border border-blue-200 rounded-lg p-3 text-sm';
        
        let metadataHTML = `<div class="font-semibold text-blue-900 mb-2">📦 Package Information</div>`;
        metadataHTML += `<div class="text-blue-800"><strong>Tracking:</strong> ${metadata.trackingNumber}</div>`;
        
        if (metadata.status) {
            const statusColor = this.getStatusColor(metadata.status);
            metadataHTML += `<div class="text-blue-800 mt-1"><strong>Status:</strong> <span class="px-2 py-1 rounded ${statusColor}">${metadata.status}</span></div>`;
        }
        
        if (metadata.message) {
            metadataHTML += `<div class="text-blue-700 mt-2 text-xs">${this.escapeHtml(metadata.message)}</div>`;
        }
        
        metadataDiv.innerHTML = metadataHTML;
        messageDiv.appendChild(metadataDiv);
    }

    showAgentHandoffOption(messageDiv) {
        const handoffDiv = document.createElement('div');
        handoffDiv.className = 'mt-2 ml-10 bg-yellow-50 border border-yellow-200 rounded-lg p-3';
        handoffDiv.innerHTML = `
            <div class="flex items-center justify-between">
                <div class="flex items-center space-x-2">
                    <i class="fas fa-user-headset text-yellow-600"></i>
                    <span class="text-sm text-yellow-900">Would you like to speak with a human agent?</span>
                </div>
                <button onclick="requestHumanAgent()" class="ml-4 px-4 py-1 bg-yellow-600 hover:bg-yellow-700 text-white text-sm rounded-lg transition-colors">
                    Connect to Agent
                </button>
            </div>
        `;
        messageDiv.appendChild(handoffDiv);
    }

    showTypingIndicator() {
        this.isTyping = true;
        const messagesContainer = document.getElementById('chatMessages');
        if (!messagesContainer) return;

        // Remove existing typing indicator
        const existingTyping = messagesContainer.querySelector('.typing-indicator');
        if (existingTyping) existingTyping.remove();

        const typingDiv = document.createElement('div');
        typingDiv.className = 'typing-indicator flex justify-start mb-4';
        typingDiv.innerHTML = `
            <div class="flex items-start space-x-2">
                <div class="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
                    <i class="fas fa-robot text-blue-600 text-sm"></i>
                </div>
                <div class="bg-white border border-gray-200 rounded-lg px-4 py-2">
                    <div class="flex space-x-1">
                        <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 0s"></div>
                        <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 0.2s"></div>
                        <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 0.4s"></div>
                    </div>
                </div>
            </div>
        `;
        messagesContainer.appendChild(typingDiv);
        this.scrollToBottom();
    }

    hideTypingIndicator() {
        this.isTyping = false;
        const messagesContainer = document.getElementById('chatMessages');
        if (!messagesContainer) return;

        const typingIndicator = messagesContainer.querySelector('.typing-indicator');
        if (typingIndicator) {
            typingIndicator.remove();
        }
    }

    showWelcomeMessage() {
        const messagesContainer = document.getElementById('chatMessages');
        if (!messagesContainer || this.messages.length > 0) return;

        const welcomeDiv = document.createElement('div');
        welcomeDiv.className = 'empty-state text-center py-8';
        welcomeDiv.innerHTML = `
            <div class="bg-blue-50 border border-blue-200 rounded-lg p-6 mb-4">
                <div class="text-4xl mb-2">👋</div>
                <h3 class="font-semibold text-gray-900 mb-2">Welcome to Reliable Carriers Support!</h3>
                <p class="text-sm text-gray-600 mb-4">I'm your AI assistant. I can help you with:</p>
                <div class="grid grid-cols-2 gap-2 text-xs text-left">
                    <div class="flex items-center space-x-2">
                        <i class="fas fa-search text-blue-600"></i>
                        <span>Package tracking</span>
                    </div>
                    <div class="flex items-center space-x-2">
                        <i class="fas fa-calculator text-blue-600"></i>
                        <span>Get quotes</span>
                    </div>
                    <div class="flex items-center space-x-2">
                        <i class="fas fa-clock text-blue-600"></i>
                        <span>Delivery times</span>
                    </div>
                    <div class="flex items-center space-x-2">
                        <i class="fas fa-question-circle text-blue-600"></i>
                        <span>FAQ & Support</span>
                    </div>
                </div>
            </div>
        `;
        messagesContainer.appendChild(welcomeDiv);
    }

    setupUI() {
        const sendButton = document.getElementById('chatSendButton');
        const messageInput = document.getElementById('chatMessageInput');

        if (sendButton) {
            sendButton.addEventListener('click', () => {
                if (messageInput) {
                    this.sendMessage(messageInput.value);
                }
            });
        }

        if (messageInput) {
            messageInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault();
                    this.sendMessage(messageInput.value);
                }
            });

            // Auto-resize textarea
            messageInput.addEventListener('input', function() {
                this.style.height = 'auto';
                this.style.height = Math.min(this.scrollHeight, 120) + 'px';
            });
        }
    }

    scrollToBottom() {
        const messagesContainer = document.getElementById('chatMessages');
        if (messagesContainer) {
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        }
    }

    updateConnectionStatus(connected) {
        const statusIndicator = document.getElementById('chatConnectionStatus');
        if (statusIndicator) {
            statusIndicator.className = connected 
                ? 'w-2 h-2 bg-green-500 rounded-full mr-2 animate-pulse' 
                : 'w-2 h-2 bg-red-500 rounded-full mr-2';
            statusIndicator.title = connected ? 'Connected' : 'Disconnected';
        }
    }

    formatTime(timestamp) {
        if (!timestamp) return '';
        const date = new Date(timestamp);
        const now = new Date();
        const diff = now - date;
        const minutes = Math.floor(diff / 60000);

        if (minutes < 1) return 'Just now';
        if (minutes < 60) return `${minutes}m ago`;
        if (minutes < 1440) return `${Math.floor(minutes / 60)}h ago`;
        return date.toLocaleDateString('en-ZA', { hour: '2-digit', minute: '2-digit' });
    }

    getStatusColor(status) {
        const statusUpper = status.toUpperCase();
        if (statusUpper.includes('DELIVERED')) return 'bg-green-100 text-green-800';
        if (statusUpper.includes('TRANSIT') || statusUpper.includes('DELIVERY')) return 'bg-blue-100 text-blue-800';
        if (statusUpper.includes('PENDING') || statusUpper.includes('CONFIRMED')) return 'bg-yellow-100 text-yellow-800';
        return 'bg-gray-100 text-gray-800';
    }

    escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    showError(message) {
        const toast = document.createElement('div');
        toast.className = 'fixed bottom-4 right-4 bg-red-500 text-white px-6 py-3 rounded-lg shadow-lg z-50';
        toast.textContent = message;
        document.body.appendChild(toast);

        setTimeout(() => {
            toast.remove();
        }, 3000);
    }

    disconnect() {
        if (this.stompClient) {
            this.stompClient.disconnect();
        }
        this.hideTypingIndicator();
    }
}

// Global chat instance
let chatSystem = null;

// Initialize chat
async function initChat(chatId, userEmail) {
    if (chatSystem) {
        chatSystem.disconnect();
    }
    chatSystem = new EnhancedChatSystem(chatId, userEmail);
    await chatSystem.init();
}

// Initiate new chat
async function initiateChat(customerEmail, trackingNumber, subject) {
    try {
        const response = await fetch('/api/chat/initiate', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                customerEmail: customerEmail,
                trackingNumber: trackingNumber,
                subject: subject || 'General Inquiry'
            })
        });

        const result = await response.json();

        if (result.success && result.chatId) {
            await initChat(result.chatId, customerEmail);
            return result.chatId;
        } else {
            throw new Error(result.message || 'Failed to initiate chat');
        }
    } catch (error) {
        console.error('Error initiating chat:', error);
        throw error;
    }
}

// Request human agent
function requestHumanAgent() {
    if (chatSystem) {
        chatSystem.sendMessage('I would like to speak with a human agent, please.');
    }
    // In production, this would trigger an escalation to support queue
    alert('An agent will be with you shortly. Thank you for your patience.');
}

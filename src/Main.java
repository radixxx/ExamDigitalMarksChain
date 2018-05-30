import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Main {

    public static ArrayList<Block> blockchain = new ArrayList<Block>();
    public static HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();
    public static int difficulty = 3;
    public static float minimumTransaction = 0.1f;
    public static WalletDiary walletDiaryTeacher;//wallet diary
    public static WalletDiary walletDiaryB;
    public static WalletDiary walletDiaryC;
    public static WalletDiary walletDiaryD;
    public static WalletDiary walletDiaryJ;

    public static Transaction genesisTransaction;

    public static void main(String[] args) {
        //add our blocks to the blockchain ArrayList:
        //Setup Bouncey castle as a Security Provider
        //Logger.Set(LOggerr.creteGuiLogger());
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        //Create walletsDiary's:
        walletDiaryTeacher = new WalletDiary();
        walletDiaryB = new WalletDiary();
        walletDiaryC = new WalletDiary();
        walletDiaryD = new WalletDiary();
        walletDiaryJ = new WalletDiary();
        WalletDiary coinbase = new WalletDiary();


        //create genesis transaction, which sends X Coin's to walletDiaryTeacher:
        System.out.println("Enter value: ");
        Scanner in = new Scanner(System.in);
        float value = in.nextFloat();

        genesisTransaction = new Transaction(coinbase.publicKey, walletDiaryTeacher.publicKey, value, null);
        genesisTransaction.generateSignature(coinbase.privateKey);	 //manually sign the genesis transaction
        genesisTransaction.transactionId = "0"; //manually set the transaction id
        //manually add the Transactions Output
        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.reciepient, genesisTransaction.value, genesisTransaction.transactionId));
        UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); //its important to store our first transaction in the UTXOs list.

        System.out.println("Creating and Mining Genesis block...+ ");
        Block genesis = new Block("0");
        genesis.addTransaction(genesisTransaction);
        addBlock(genesis);

        //testing
        Block block1 = new Block(genesis.hash);
        System.out.println("\nwalletDiaryTeacher's balance is: " + walletDiaryTeacher.getBalance());
        System.out.println("\nwalletDiaryTeacher is Attempting to send mark to WalletB...");//value
        block1.addTransaction(walletDiaryTeacher.sendFunds(walletDiaryB.publicKey, 10));
        addBlock(block1);
        System.out.println("\nwalletDiaryTeacher's balance is: " + walletDiaryTeacher.getBalance());
        System.out.println("WalletB's balance is: " + walletDiaryB.getBalance());

        Block block2 = new Block(block1.hash);
        System.out.println("\nwalletDiaryTeacher Attempting to send more mark's (10)");
        block2.addTransaction(walletDiaryTeacher.sendFunds(walletDiaryB.publicKey, 10f));
        addBlock(block2);
        System.out.println("\nwalletDiaryTeacher's balance is: " + walletDiaryTeacher.getBalance());
        System.out.println("WalletB's balance is: " + walletDiaryB.getBalance());

        Block block3 = new Block(block2.hash);
        System.out.println("\nWalletB is Attempting to send mark (8) to WalletB...");
        block3.addTransaction(walletDiaryTeacher.sendFunds( walletDiaryB.publicKey, 80));//20
        addBlock(block3);
        System.out.println("\nwalletDiaryTeacher's balance is: " + walletDiaryTeacher.getBalance());
        System.out.println("WalletB's balance is: " + walletDiaryB.getBalance());

        //added my new custom new block
        Block block4 = new Block(block3.hash);
        System.out.println("\nwalletDiaryTeacher's is Attempting to send mark (10) to WalletC...");
        block4.addTransaction(walletDiaryTeacher.sendFunds( walletDiaryC.publicKey, 10));
        addBlock(block4);
        System.out.println("\nwalletDiaryTeacher's balance is: " + walletDiaryTeacher.getBalance());
        System.out.println("\nWalletC's balance is: " + walletDiaryC.getBalance());


        //added my new custom new block
        Block block5 = new Block(block4.hash);
        System.out.println("\nwalletDiaryTeacher is Attempting to send mark (10) to WalletJ...");
        block4.addTransaction(walletDiaryTeacher.sendFunds( walletDiaryJ.publicKey, 9));//10
        addBlock(block5);
        System.out.println("\nwalletDiaryTeacher's balance is: " + walletDiaryTeacher.getBalance());
        System.out.println("\nWalletJ's balance is: " + walletDiaryJ.getBalance());


        isChainValid();

    }

    public static Boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>(); //a temporary working list of unspent transactions at a given block state.
        tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        //loop through blockchain to check hashes:
        for(int i=1; i < blockchain.size(); i++) {

            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i-1);
            //compare registered hash and calculated hash:
            if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
                System.out.println("#Current Hashes not equal");
                return false;
            }
            //compare previous hash and registered previous hash
            if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
                System.out.println("#Previous Hashes not equal");
                return false;
            }
            //check if hash is solved
            if(!currentBlock.hash.substring( 0, difficulty).equals(hashTarget)) {
                System.out.println("#This block hasn't been mined");
                return false;
            }

            //loop thru blockchains transactions:
            TransactionOutput tempOutput;
            for(int t=0; t <currentBlock.transactions.size(); t++) {
                Transaction currentTransaction = currentBlock.transactions.get(t);

                if(!currentTransaction.verifySignature()) {
                    System.out.println("#Signature on Transaction(" + t + ") is Invalid");
                    return false;
                }
                if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
                    System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
                    return false;
                }

                for(TransactionInput input: currentTransaction.inputs) {
                    tempOutput = tempUTXOs.get(input.transactionOutputId);

                    if(tempOutput == null) {
                        System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
                        return false;
                    }

                    if(input.UTXO.value != tempOutput.value) {
                        System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
                        return false;
                    }

                    tempUTXOs.remove(input.transactionOutputId);
                }

                for(TransactionOutput output: currentTransaction.outputs) {
                    tempUTXOs.put(output.id, output);
                }

                if( currentTransaction.outputs.get(0).reciepient != currentTransaction.reciepient) {
                    System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
                    return false;
                }
                if( currentTransaction.outputs.get(1).reciepient != currentTransaction.sender) {
                    System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
                    return false;
                }

            }

        }
        System.out.println("\tBlockchain is valid");
        return true;
    }

    public static void addBlock(Block newBlock) {
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
    }

}


